package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.AssetBorrowing;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssetAccessService {

    private final MemberRepository memberRepository;

    public String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw new AccessDeniedException("User not authenticated");
        }
        return auth.getName();
    }

    public Member getCurrentMember() {
        return memberRepository.findByEmailIgnoreCase(getCurrentEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found for current user. Member record is required to perform this action."));
    }

    public Optional<Member> getCurrentMemberOptional() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            return Optional.empty();
        }
        return memberRepository.findByEmailIgnoreCase(auth.getName());
    }

    public boolean isGlobalAssetManager() {
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

    public void validateBorrowingOwner(AssetBorrowing borrowing) {
        String email = getCurrentEmail();
        if (!borrowing.getBorrowerEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Anda tidak memiliki akses terhadap peminjaman ini.");
        }
    }

    public void validateBorrowingReadAccess(AssetBorrowing borrowing) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("User not authenticated");

        boolean canReadAll = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("asset.borrow.read.all"));

        if (canReadAll) {
            return;
        }

        String email = getCurrentEmail();
        if (borrowing.getBorrowerEmail().equalsIgnoreCase(email)) {
            return;
        }

        throw new AccessDeniedException("Anda tidak memiliki akses terhadap data aset ini.");
    }

    public void validateAssetOperationalPermission() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new AccessDeniedException("User not authenticated");

        boolean hasManage = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("asset.manage"));
        
        if (!hasManage) {
            throw new AccessDeniedException("Anda tidak memiliki izin operasional aset.");
        }
    }
}
