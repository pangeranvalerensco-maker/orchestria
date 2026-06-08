package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicMemberAssignmentResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicMemberResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicOrganizationStructureResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.OrganizationPeriodRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.PublicOrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublicOrganizationServiceImpl implements PublicOrganizationService {

    private final OrganizationPeriodRepository periodRepository;
    private final MemberRepository memberRepository;
    private final MemberAssignmentRepository assignmentRepository;

    @Override
    public ApiResponse<List<OrganizationPeriodResponse>> getPublicPeriods() {
        List<OrganizationPeriodResponse> periods = periodRepository.findByPublicVisibleTrueOrderByStartDateDesc()
                .stream()
                .map(this::mapPeriodToResponse)
                .toList();

        return ApiResponse.<List<OrganizationPeriodResponse>>builder()
                .success(true)
                .message("Daftar periode publik berhasil diambil")
                .data(periods)
                .build();
    }

    @Override
    public ApiResponse<OrganizationPeriodResponse> getCurrentPeriod() {
        OrganizationPeriod period = findCurrentPublicPeriod();

        return ApiResponse.<OrganizationPeriodResponse>builder()
                .success(true)
                .message("Periode aktif publik berhasil diambil")
                .data(mapPeriodToResponse(period))
                .build();
    }

    @Override
    public ApiResponse<List<PublicMemberResponse>> getPublicMembersByCohort(String cohort) {
        List<PublicMemberResponse> members = memberRepository
                .findByCohortAndPublicVisibleTrueAndActiveTrueOrderByDisplayOrderAscFullNameAsc(cohort)
                .stream()
                .map(this::mapMemberToPublicResponse)
                .toList();

        return ApiResponse.<List<PublicMemberResponse>>builder()
                .success(true)
                .message("Daftar anggota publik berhasil diambil")
                .data(members)
                .build();
    }

    @Override
    public ApiResponse<PublicOrganizationStructureResponse> getCurrentStructure() {
        OrganizationPeriod period = findCurrentPublicPeriod();

        return ApiResponse.<PublicOrganizationStructureResponse>builder()
                .success(true)
                .message("Struktur organisasi aktif berhasil diambil")
                .data(buildStructureResponse(period))
                .build();
    }

    @Override
    public ApiResponse<PublicOrganizationStructureResponse> getStructureByPeriod(Long periodId) {
        OrganizationPeriod period = periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("Periode publik tidak ditemukan"));

        if (Boolean.FALSE.equals(period.getPublicVisible()) || Boolean.FALSE.equals(period.getActive())) {
            throw new ResourceNotFoundException("Periode publik tidak ditemukan");
        }

        return ApiResponse.<PublicOrganizationStructureResponse>builder()
                .success(true)
                .message("Struktur organisasi berdasarkan periode berhasil diambil")
                .data(buildStructureResponse(period))
                .build();
    }

    private OrganizationPeriod findCurrentPublicPeriod() {
        OrganizationPeriod period = periodRepository.findByCurrentPeriodTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Periode aktif publik tidak ditemukan"));

        if (Boolean.FALSE.equals(period.getPublicVisible()) || Boolean.FALSE.equals(period.getActive())) {
            throw new ResourceNotFoundException("Periode aktif publik tidak ditemukan");
        }

        return period;
    }

    private PublicOrganizationStructureResponse buildStructureResponse(OrganizationPeriod period) {
        List<PublicMemberAssignmentResponse> structure = assignmentRepository
                .findByPeriodAndStatusAndActiveTrueOrderByPositionLevelOrderAscMemberFullNameAsc(
                        period,
                        AssignmentStatus.ACTIVE
                )
                .stream()
                .filter(assignment -> Boolean.TRUE.equals(assignment.getMember().getPublicVisible()))
                .filter(assignment -> Boolean.TRUE.equals(assignment.getDivision().getPublicVisible()))
                .filter(assignment -> Boolean.TRUE.equals(assignment.getPosition().getPublicVisible()))
                .map(this::mapAssignmentToPublicResponse)
                .toList();

        return PublicOrganizationStructureResponse.builder()
                .period(mapPeriodToResponse(period))
                .structure(structure)
                .build();
    }

    private OrganizationPeriodResponse mapPeriodToResponse(OrganizationPeriod period) {
        return OrganizationPeriodResponse.builder()
                .id(period.getId())
                .name(period.getName())
                .startDate(period.getStartDate())
                .endDate(period.getEndDate())
                .currentPeriod(period.getCurrentPeriod())
                .publicVisible(period.getPublicVisible())
                .active(period.getActive())
                .createdAt(period.getCreatedAt())
                .updatedAt(period.getUpdatedAt())
                .build();
    }

    private PublicMemberResponse mapMemberToPublicResponse(Member member) {
        return PublicMemberResponse.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .studentNumber(member.getStudentNumber())
                .cohort(member.getCohort())
                .profilePhotoUrl(member.getProfilePhotoUrl())
                .major(member.getMajor())
                .campusClass(member.getCampusClass())
                .build();
    }

    private PublicMemberAssignmentResponse mapAssignmentToPublicResponse(MemberAssignment assignment) {
        return PublicMemberAssignmentResponse.builder()
                .memberId(assignment.getMember().getId())
                .memberName(assignment.getMember().getFullName())
                .cohort(assignment.getMember().getCohort())
                .profilePhotoUrl(assignment.getMember().getProfilePhotoUrl())
                .major(assignment.getMember().getMajor())
                .periodId(assignment.getPeriod().getId())
                .periodName(assignment.getPeriod().getName())
                .divisionId(assignment.getDivision().getId())
                .divisionCode(assignment.getDivision().getCode())
                .divisionName(assignment.getDivision().getName())
                .positionId(assignment.getPosition().getId())
                .positionCode(assignment.getPosition().getCode())
                .positionName(assignment.getPosition().getName())
                .positionLevelOrder(assignment.getPosition().getLevelOrder())
                .build();
    }
}