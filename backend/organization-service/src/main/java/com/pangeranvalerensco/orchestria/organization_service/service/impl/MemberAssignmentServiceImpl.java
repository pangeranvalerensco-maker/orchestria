package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Division;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.MemberAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.OrganizationPeriod;
import com.pangeranvalerensco.orchestria.organization_service.entity.Position;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberAssignmentRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberAssignmentResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.DivisionRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.OrganizationPeriodRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.PositionRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.MemberAssignmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberAssignmentServiceImpl implements MemberAssignmentService {

    private final MemberAssignmentRepository assignmentRepository;
    private final MemberRepository memberRepository;
    private final OrganizationPeriodRepository periodRepository;
    private final DivisionRepository divisionRepository;
    private final PositionRepository positionRepository;

    @Override
    public ApiResponse<List<MemberAssignmentResponse>> getAllAssignment() {
        List<MemberAssignmentResponse> assignments = assignmentRepository.findByActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<MemberAssignmentResponse>>builder()
                .success(true)
                .message("Daftar penugasan anggota berhasil diambil")
                .data(assignments)
                .build();
    }

    @Override
    public ApiResponse<List<MemberAssignmentResponse>> getAssignmentByPeriod(Long periodId) {
        OrganizationPeriod period = findPeriodById(periodId);

        List<MemberAssignmentResponse> assignments = assignmentRepository
                .findByPeriodAndStatusAndActiveTrueOrderByPositionLevelOrderAscMemberFullNameAsc(
                        period,
                        AssignmentStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<MemberAssignmentResponse>>builder()
                .success(true)
                .message("Daftar Struktur Periode Berhasil diambil")
                .data(assignments)
                .build();
    }

    @Override
    public ApiResponse<List<MemberAssignmentResponse>> getAssignmentByPeriodAndDivision(Long periodId,
            Long divisionId) {
        OrganizationPeriod period = findPeriodById(periodId);
        Division division = findDivisionById(divisionId);

        List<MemberAssignmentResponse> assignments = assignmentRepository
                .findByPeriodAndDivisionAndStatusAndActiveTrueOrderByPositionLevelOrderAscMemberFullNameAsc(
                        period,
                        division,
                        AssignmentStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return ApiResponse.<List<MemberAssignmentResponse>>builder()
                .success(true)
                .message("Daftar struktur Divisi berhasil diambil")
                .data(assignments)
                .build();
    }

    @Override
    public ApiResponse<MemberAssignmentResponse> getAssignmentById(Long id) {
        MemberAssignment assignment = findAssignmentById(id);

        return ApiResponse.<MemberAssignmentResponse>builder()
                .success(true)
                .message("Daftar penugasan anggota berhasil diambil")
                .data(mapToResponse(assignment))
                .build();
    }

    @Override
    public ApiResponse<MemberAssignmentResponse> createAssignment(MemberAssignmentRequest request) {
        Member member = findMemberById(request.getMemberId());
        OrganizationPeriod period = findPeriodById(request.getPeriodId());
        Division division = findDivisionById(request.getDivisionId());
        Position position = findPositionById(request.getPositionId());

        if (assignmentRepository.existsByMemberAndPeriodAndDivisionAndPositionAndActiveTrue(
                member,
                period,
                division,
                position)) {
            throw new BadRequestException("Penugasan anggota tersebut sudah ada");
        }

        MemberAssignment assignment = MemberAssignment.builder()
                .member(member)
                .period(period)
                .division(division)
                .position(position)
                .status(defaultIfNull(request.getStatus(), AssignmentStatus.ACTIVE))
                .active(true)
                .build();

        MemberAssignment savedAssignment = assignmentRepository.save(assignment);

        return ApiResponse.<MemberAssignmentResponse>builder()
                .success(true)
                .message("Penugasan anggota berhasil dibuat")
                .data(mapToResponse(savedAssignment))
                .build();
    }

    @Override
    public ApiResponse<MemberAssignmentResponse> updateAssignment(Long id, MemberAssignmentRequest request) {
        MemberAssignment assignment = findAssignmentById(id);

        Member member = findMemberById(id);
        OrganizationPeriod period = findPeriodById(id);
        Division division = findDivisionById(id);
        Position position = findPositionById(id);

        if (!assignment.getMember().getId().equals(member.getId())
                || !assignment.getPeriod().getId().equals(period.getId())
                || !assignment.getDivision().getId().equals(division.getId())
                || !assignment.getPosition().getId().equals(position.getId())) {

            if(assignmentRepository.existsByMemberAndPeriodAndDivisionAndPositionAndActiveTrue(
                    member, 
                    period, 
                    division, 
                    position
            )) {
                throw new BadRequestException("Penugasan anggota tersebut sudah ada");
            }
        }

        assignment.setMember(member);
        assignment.setPeriod(period);
        assignment.setDivision(division);
        assignment.setPosition(position);
        assignment.setStatus(defaultIfNull(request.getStatus(), AssignmentStatus.ACTIVE));

        MemberAssignment savedAssignment = assignmentRepository.save(assignment);

        return ApiResponse.<MemberAssignmentResponse>builder()
                .success(true)
                .message("Penugasan anggota berhasil diperbarui")
                .data(mapToResponse(savedAssignment))
                .build();
    }

    @Override
    public ApiResponse<Void> deleteAssignment(Long id) {
        MemberAssignment assignment = findAssignmentById(id);
        assignment.setActive(false);
        assignment.setStatus(AssignmentStatus.INACTIVE);
        assignmentRepository.save(assignment);

        return ApiResponse.<Void>builder()
                .success(true)
                .message("Penugasan anggota berhasil dinonaktifkan")
                .data(null)
                .build();
    }

    private MemberAssignment findAssignmentById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Penugasan anggota tidak ditemukan"));
    }

    private Member findMemberById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anggota tidak ditemukan"));
    }

    private OrganizationPeriod findPeriodById(Long id) {
        return periodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Periode organisasi tidak ditemukan"));
    }

    private Division findDivisionById(Long id) {
        return divisionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Divisi tidak ditemukan"));
    }

    private Position findPositionById(Long id) {
        return positionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jabatan tidak ditemukan"));
    }

    private MemberAssignmentResponse mapToResponse(MemberAssignment assignment) {
        Member member = assignment.getMember();
        OrganizationPeriod period = assignment.getPeriod();
        Division division = assignment.getDivision();
        Position position = assignment.getPosition();

        return MemberAssignmentResponse.builder()
                .id(assignment.getId())
                .memberId(member.getId())
                .memberName(member.getFullName())
                .memberEmail(member.getEmail())
                .cohort(member.getCohort())
                .periodId(period.getId())
                .periodName(period.getName())
                .divisionId(division.getId())
                .divisionCode(division.getCode())
                .divisionName(division.getName())
                .positionId(position.getId())
                .positionCode(position.getCode())
                .positionName(position.getName())
                .positionLevelOrder(position.getLevelOrder())
                .status(assignment.getStatus())
                .active(assignment.getActive())
                .createdAt(assignment.getCreatedAt())
                .updatedAt(assignment.getUpdatedAt())
                .build();
    }

    private <T> T defaultIfNull(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }

}
