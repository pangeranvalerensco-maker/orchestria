package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.MemberService;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.CurrentMemberContextResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberAssignmentResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MemberAssignmentRepository memberAssignmentRepository;

    @Override
    public ApiResponse<List<MemberResponse>> getAllMembers() {
        List<MemberResponse> members = memberRepository.findAll()
                .stream()
                .filter(Member::getActive)
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<MemberResponse>>builder()
                .success(true)
                .message("Daftar Anggota berhasil diambil")
                .data(members)
                .build();
    }

    @Override
    public ApiResponse<MemberResponse> getMemberById(Long id) {
        Member member = findMemberById(id);

        return ApiResponse.<MemberResponse>builder()
                .success(true)
                .message("Detail Anggota berhasil diambil")
                .data(mapToResponse(member))
                .build();
    }

    @Override
    public ApiResponse<MemberResponse> createMember(MemberRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();

        if (memberRepository.existsByEmail(email)) {
            throw new BadRequestException("Email anggota sudah digunakan");
        }

        if (request.getStudentNumber() != null
                && !request.getStudentNumber().isBlank()
                && memberRepository.existsByStudentNumber(request.getStudentNumber().trim())) {
            throw new BadRequestException("NIM Anggota sudah digunakan");
        }

        Member member = Member.builder()
                .authUserId(request.getAuthUserId())
                .fullName(fullName)
                .email(email)
                .studentNumber(trimOrNull(request.getStudentNumber()))
                .phoneNumber(trimOrNull(request.getPhoneNumber()))
                .cohort(trimOrNull(request.getCohort()))
                .profilePhotoUrl(trimOrNull(request.getProfilePhotoUrl()))
                .major(trimOrNull(request.getMajor()))
                .campusClass(trimOrNull(request.getCampusClass()))
                .publicVisible(defaultIfNull(request.getPublicVisible(), true))
                .displayOrder(defaultIfNull(request.getDisplayOrder(), 99))
                .status(request.getStatus())
                .active(true)
                .build();

        Member savedMember = memberRepository.save(member);

        return ApiResponse.<MemberResponse>builder()
                .success(true)
                .message("Anggota berhasil dibuat")
                .data(mapToResponse(savedMember))
                .build();
    }

    @Override
    public ApiResponse<MemberResponse> updateMember(Long id, MemberRequest request) {
        Member member = findMemberById(id);

        String email = request.getEmail().trim().toLowerCase();
        String fullName = request.getFullName().trim();

        memberRepository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BadRequestException("Email anggota sudah digunakan");
            }
        });

        String studentNumber = trimOrNull(request.getStudentNumber());
        if (studentNumber != null) {
            memberRepository.findByStudentNumber(studentNumber).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new BadRequestException("NIM Anggota sudah digunakan");
                }
            });
        }

        member.setAuthUserId(request.getAuthUserId());
        member.setFullName(fullName);
        member.setEmail(email);
        member.setStudentNumber(studentNumber);
        member.setPhoneNumber(trimOrNull(request.getPhoneNumber()));
        member.setCohort(trimOrNull(request.getCohort()));
        member.setProfilePhotoUrl(trimOrNull(request.getProfilePhotoUrl()));
        member.setMajor(trimOrNull(request.getMajor()));
        member.setCampusClass(trimOrNull(request.getCampusClass()));
        member.setPublicVisible(defaultIfNull(request.getPublicVisible(), true));
        member.setDisplayOrder(defaultIfNull(request.getDisplayOrder(), 99));
        member.setStatus(request.getStatus());

        Member savedMember = memberRepository.save(member);

        return ApiResponse.<MemberResponse>builder()
                .success(true)
                .message("Anggota berhasil diperbarui")
                .data(mapToResponse(savedMember))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteMember(Long id) {
        Member member = findMemberById(id);
        member.setActive(false);
        memberRepository.save(member);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Anggota berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    @Override
    public ApiResponse<CurrentMemberContextResponse> getCurrentMemberContext(String email) {
        Member member = memberRepository.findByEmailIgnoreCase(email)
                .filter(Member::getActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Data anggota untuk user login tidak ditemukan"));

        List<MemberAssignmentResponse> assignments = memberAssignmentRepository
                .findByMemberAndStatusAndActiveTrueAndPeriodCurrentPeriodTrueAndPeriodActiveTrue(
                        member,
                        AssignmentStatus.ACTIVE)
                .stream()
                .map(this::mapAssignmentToResponse)
                .toList();

        CurrentMemberContextResponse context = CurrentMemberContextResponse.builder()
                .member(mapToResponse(member))
                .activeAssignments(assignments)
                .build();

        return ApiResponse.<CurrentMemberContextResponse>builder()
                .success(true)
                .message("Context anggota login berhasil diambil")
                .data(context)
                .build();
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anggota tidak ditemukan"));
    }

    private MemberResponse mapToResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .authUserId(member.getAuthUserId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .studentNumber(member.getStudentNumber())
                .phoneNumber(member.getPhoneNumber())
                .cohort(member.getCohort())
                .profilePhotoUrl(member.getProfilePhotoUrl())
                .major(member.getMajor())
                .campusClass(member.getCampusClass())
                .publicVisible(member.getPublicVisible())
                .displayOrder(member.getDisplayOrder())
                .status(member.getStatus())
                .active(member.getActive())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }

    private String trimOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

    private MemberAssignmentResponse mapAssignmentToResponse(
            MemberAssignment assignment) {
        return MemberAssignmentResponse.builder()
                .id(assignment.getId())
                .memberId(assignment.getMember().getId())
                .memberName(assignment.getMember().getFullName())
                .memberEmail(assignment.getMember().getEmail())
                .cohort(assignment.getMember().getCohort())
                .periodId(assignment.getPeriod().getId())
                .periodName(assignment.getPeriod().getName())
                .divisionId(assignment.getDivision().getId())
                .divisionCode(assignment.getDivision().getCode())
                .divisionName(assignment.getDivision().getName())
                .positionId(assignment.getPosition().getId())
                .positionCode(assignment.getPosition().getCode())
                .positionName(assignment.getPosition().getName())
                .positionLevelOrder(
                        assignment.getPosition().getLevelOrder())
                .status(assignment.getStatus())
                .active(assignment.getActive())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

}
