package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.DivisionTask;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DivisionTaskAccessService {

    private final MemberRepository memberRepository;
    private final MemberAssignmentRepository memberAssignmentRepository;

    public Member getCurrentMember() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new AccessDeniedException("User not authenticated");
        }
        return memberRepository.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found for current user"));
    }

    public boolean isGlobalManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_SUPER_ADMIN") || 
                authority.getAuthority().equals("ROLE_KETUA_PUB")) {
                return true;
            }
        }
        return false;
    }

    public boolean isKetuaDivisi() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        
        for (GrantedAuthority authority : auth.getAuthorities()) {
            if (authority.getAuthority().equals("ROLE_KETUA_DIVISI")) {
                return true;
            }
        }
        return false;
    }

    public List<Long> getManagedDivisionIds() {
        if (isGlobalManager()) {
            return null; // Null indicates all divisions are managed
        }

        if (isKetuaDivisi()) {
            Member currentMember = getCurrentMember();
            List<MemberAssignment> activeAssignments = memberAssignmentRepository
                    .findByMemberAndStatusAndActiveTrueAndPeriodCurrentPeriodTrueAndPeriodActiveTrue(
                            currentMember, AssignmentStatus.ACTIVE);
            
            return activeAssignments.stream()
                    .map(a -> a.getDivision().getId())
                    .collect(Collectors.toList());
        }

        return List.of(); // Empty list means no divisions managed
    }

    public void validateManagerAccess(Long divisionId) {
        if (isGlobalManager()) return;

        List<Long> managedDivisionIds = getManagedDivisionIds();
        if (managedDivisionIds == null) return; // Global manager

        if (!managedDivisionIds.contains(divisionId)) {
            throw new AccessDeniedException("You do not have manager access to this division");
        }
    }

    public boolean isDivisionManagerFor(Long divisionId) {
        if (isGlobalManager()) return true;
        List<Long> managed = getManagedDivisionIds();
        return managed != null && managed.contains(divisionId);
    }

    public void validateTaskReadAccess(DivisionTask task) {
        if (isGlobalManager()) return;

        Member currentMember = getCurrentMember();

        if (task.getAssignedMember() != null && task.getAssignedMember().getId().equals(currentMember.getId())) {
            return;
        }

        if (isDivisionManagerFor(task.getDivision().getId())) {
            return;
        }

        throw new AccessDeniedException("Anda tidak memiliki akses terhadap tugas ini.");
    }

    public void validateTaskAssignment(DivisionTask task) {
        Member currentMember = getCurrentMember();
        if (task.getAssignedMember() == null || !task.getAssignedMember().getId().equals(currentMember.getId())) {
            throw new AccessDeniedException("You are not assigned to this task");
        }
    }
}
