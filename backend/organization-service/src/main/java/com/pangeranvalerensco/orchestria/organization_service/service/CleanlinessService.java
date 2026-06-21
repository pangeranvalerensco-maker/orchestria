package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessPointRecord;
import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessSchedule;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.ScheduleStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.AttendanceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.PointRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.ScheduleRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.cleanliness.CleanlinessResponses.*;
import com.pangeranvalerensco.orchestria.organization_service.repository.CleanlinessAssignmentRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.CleanlinessPointRecordRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.CleanlinessScheduleRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CleanlinessService {

    private final CleanlinessScheduleRepository scheduleRepository;
    private final CleanlinessAssignmentRepository assignmentRepository;
    private final CleanlinessPointRecordRepository pointRepository;
    private final MemberRepository memberRepository;

    private String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User not authenticated");
        }
        return auth.getName();
    }

    private Member getCurrentMember() {
        return memberRepository.findByEmailIgnoreCase(getCurrentEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found for current user"));
    }

    private boolean isManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("cleanliness.schedule.manage") || a.equals("ROLE_SUPER_ADMIN"));
    }

    // ==========================================
    // SCHEDULE MANAGEMENT
    // ==========================================

    @Transactional
    public ScheduleResponse createSchedule(ScheduleRequest request) {
        if (request.endTime().isBefore(request.startTime())) {
            throw new BadRequestException("Waktu selesai tidak boleh sebelum waktu mulai.");
        }

        CleanlinessSchedule schedule = CleanlinessSchedule.builder()
                .title(request.title())
                .dutyDate(request.dutyDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .location(request.location())
                .description(request.description())
                .status(request.status())
                .active(true)
                .createdByEmail(getCurrentEmail())
                .build();

        schedule = scheduleRepository.save(schedule);

        List<CleanlinessAssignment> assignments = new ArrayList<>();
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            for (Long memberId : request.memberIds()) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan: " + memberId));
                if ((member.getActive() == null || !member.getActive())) {
                    throw new BadRequestException("Member tidak aktif: " + member.getFullName());
                }

                CleanlinessAssignment assignment = CleanlinessAssignment.builder()
                        .schedule(schedule)
                        .memberId(member.getId())
                        .memberName(member.getFullName())
                        .memberEmail(member.getEmail())
                        .attendanceStatus(AttendanceStatus.PENDING)
                        .active(true)
                        .build();
                assignments.add(assignmentRepository.save(assignment));
            }
        }

        return ScheduleResponse.fromEntity(schedule, assignments);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getAllSchedules() {
        if (isManager()) {
            return scheduleRepository.findByActiveTrueOrderByDutyDateDescStartTimeDesc().stream()
                    .map(s -> ScheduleResponse.fromEntity(s, assignmentRepository.findByScheduleIdAndActiveTrue(s.getId())))
                    .toList();
        } else {
            Member member = getCurrentMember();
            List<CleanlinessAssignment> myAssignments = assignmentRepository.findPublishedAssignmentsByMemberId(member.getId());
            return myAssignments.stream()
                    .map(CleanlinessAssignment::getSchedule)
                    .distinct()
                    .map(s -> ScheduleResponse.fromEntity(s, assignmentRepository.findByScheduleIdAndActiveTrue(s.getId())))
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedules() {
        Member member = getCurrentMember();
        List<CleanlinessAssignment> myAssignments = assignmentRepository.findByMemberIdAndActiveTrue(member.getId());
        return myAssignments.stream()
                .map(CleanlinessAssignment::getSchedule)
                .filter(s -> s.isActive() && s.getStatus() != ScheduleStatus.DRAFT)
                .distinct()
                .map(s -> ScheduleResponse.fromEntity(s, assignmentRepository.findByScheduleIdAndActiveTrue(s.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ScheduleResponse getSchedule(String id) {
        CleanlinessSchedule schedule = scheduleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal piket tidak ditemukan"));

        List<CleanlinessAssignment> assignments = assignmentRepository.findByScheduleIdAndActiveTrue(id);

        if (!isManager()) {
            if (schedule.getStatus() == ScheduleStatus.DRAFT) {
                throw new AccessDeniedException("Jadwal belum dipublikasikan");
            }
            Member member = getCurrentMember();
            boolean isAssigned = assignments.stream().anyMatch(a -> a.getMemberId().equals(member.getId()));
            if (!isAssigned) {
                throw new AccessDeniedException("Anda tidak ditugaskan pada jadwal ini");
            }
        }

        return ScheduleResponse.fromEntity(schedule, assignments);
    }

    @Transactional
    public ScheduleResponse updateSchedule(String id, ScheduleRequest request) {
        if (request.endTime().isBefore(request.startTime())) {
            throw new BadRequestException("Waktu selesai tidak boleh sebelum waktu mulai.");
        }

        CleanlinessSchedule schedule = scheduleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal piket tidak ditemukan"));

        schedule.setTitle(request.title());
        schedule.setDutyDate(request.dutyDate());
        schedule.setStartTime(request.startTime());
        schedule.setEndTime(request.endTime());
        schedule.setLocation(request.location());
        schedule.setDescription(request.description());
        schedule.setStatus(request.status());

        schedule = scheduleRepository.save(schedule);

        // Update assignments
        List<CleanlinessAssignment> existingAssignments = assignmentRepository.findByScheduleIdAndActiveTrue(id);
        
        // Soft delete removed ones
        List<Long> newMemberIds = request.memberIds() != null ? request.memberIds() : List.of();
        for (CleanlinessAssignment assignment : existingAssignments) {
            if (!newMemberIds.contains(assignment.getMemberId())) {
                assignment.setActive(false);
                assignmentRepository.save(assignment);
            }
        }

        // Add new ones
        for (Long memberId : newMemberIds) {
            boolean exists = existingAssignments.stream()
                    .anyMatch(a -> a.getMemberId().equals(memberId) && a.isActive());
            if (!exists) {
                Member member = memberRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan: " + memberId));
                if ((member.getActive() == null || !member.getActive())) {
                    throw new BadRequestException("Member tidak aktif: " + member.getFullName());
                }

                CleanlinessAssignment assignment = CleanlinessAssignment.builder()
                        .schedule(schedule)
                        .memberId(member.getId())
                        .memberName(member.getFullName())
                        .memberEmail(member.getEmail())
                        .attendanceStatus(AttendanceStatus.PENDING)
                        .active(true)
                        .build();
                assignmentRepository.save(assignment);
            }
        }

        return ScheduleResponse.fromEntity(schedule, assignmentRepository.findByScheduleIdAndActiveTrue(id));
    }

    @Transactional
    public void deleteSchedule(String id) {
        CleanlinessSchedule schedule = scheduleRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jadwal piket tidak ditemukan"));

        if (schedule.getStatus() == ScheduleStatus.PUBLISHED || schedule.getStatus() == ScheduleStatus.COMPLETED || schedule.getStatus() == ScheduleStatus.CANCELLED) {
            schedule.setStatus(ScheduleStatus.CANCELLED);
        } else {
            schedule.setActive(false);
            List<CleanlinessAssignment> assignments = assignmentRepository.findByScheduleIdAndActiveTrue(id);
            for (CleanlinessAssignment a : assignments) {
                a.setActive(false);
                assignmentRepository.save(a);
            }
        }
        scheduleRepository.save(schedule);
    }

    // ==========================================
    // ATTENDANCE MANAGEMENT
    // ==========================================

    @Transactional
    public AssignmentResponse recordAttendance(String assignmentId, AttendanceRequest request) {
        CleanlinessAssignment assignment = assignmentRepository.findByIdAndActiveTrue(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment tidak ditemukan"));

        CleanlinessSchedule schedule = assignment.getSchedule();
        if (schedule.getStatus() == ScheduleStatus.COMPLETED || schedule.getStatus() == ScheduleStatus.CANCELLED) {
            throw new BadRequestException("Tidak dapat mengubah presensi pada jadwal yang sudah selesai atau dibatalkan");
        }

        if (!isManager()) {
            Member member = getCurrentMember();
            if (!assignment.getMemberId().equals(member.getId())) {
                throw new AccessDeniedException("Anda hanya dapat mengisi presensi milik sendiri");
            }
        }

        assignment.setAttendanceStatus(request.status());
        assignment.setAttendanceNote(request.note());
        assignment.setEvidenceUrl(request.evidenceUrl());
        assignment.setAttendedAt(LocalDateTime.now());
        assignment.setRecordedByEmail(getCurrentEmail());

        return AssignmentResponse.fromEntity(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAllAttendances(String scheduleId, Long memberId, AttendanceStatus status) {
        // Find assignments matching criteria
        List<CleanlinessAssignment> assignments = assignmentRepository.findAll().stream()
                .filter(CleanlinessAssignment::isActive)
                .filter(a -> scheduleId == null || a.getSchedule().getId().equals(scheduleId))
                .filter(a -> memberId == null || a.getMemberId().equals(memberId))
                .filter(a -> status == null || a.getAttendanceStatus() == status)
                .toList();

        return assignments.stream().map(AssignmentResponse::fromEntity).toList();
    }

    // ==========================================
    // POINTS MANAGEMENT
    // ==========================================

    @Transactional
    public PointRecordResponse createPointRecord(PointRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member tidak ditemukan"));

        if (member.getActive() == null || !member.getActive()) {
            throw new BadRequestException("Member tidak aktif");
        }

        if (request.pointValue() <= 0) {
            throw new BadRequestException("Nilai poin harus positif");
        }

        CleanlinessPointRecord record = CleanlinessPointRecord.builder()
                .memberId(member.getId())
                .memberName(member.getFullName())
                .scheduleId(request.scheduleId())
                .type(request.type())
                .pointValue(request.pointValue())
                .reason(request.reason())
                .recordedByEmail(getCurrentEmail())
                .recordedAt(LocalDateTime.now())
                .active(true)
                .build();

        return PointRecordResponse.fromEntity(pointRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<PointRecordResponse> getAllPointRecords() {
        return pointRepository.findByActiveTrueOrderByRecordedAtDesc().stream()
                .map(PointRecordResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PointRecordResponse> getMyPointRecords() {
        Member member = getCurrentMember();
        return pointRepository.findByMemberIdAndActiveTrueOrderByRecordedAtDesc(member.getId()).stream()
                .map(PointRecordResponse::fromEntity)
                .toList();
    }

    // ==========================================
    // REPORTS
    // ==========================================

    @Transactional(readOnly = true)
    public ReportSummaryResponse getReportSummary() {
        long totalSchedules = scheduleRepository.count();
        long publishedSchedules = scheduleRepository.findByStatusAndActiveTrueOrderByDutyDateDescStartTimeDesc(ScheduleStatus.PUBLISHED).size();
        long completedSchedules = scheduleRepository.findByStatusAndActiveTrueOrderByDutyDateDescStartTimeDesc(ScheduleStatus.COMPLETED).size();
        
        long pendingAttendances = assignmentRepository.countByAttendanceStatusAndActiveTrue(AttendanceStatus.PENDING);
        long presentCount = assignmentRepository.countByAttendanceStatusAndActiveTrue(AttendanceStatus.PRESENT);
        long absentCount = assignmentRepository.countByAttendanceStatusAndActiveTrue(AttendanceStatus.ABSENT);
        long excusedCount = assignmentRepository.countByAttendanceStatusAndActiveTrue(AttendanceStatus.EXCUSED);

        int totalRewardPoints = pointRepository.sumTotalPointsByType(PointRecordType.REWARD);
        int totalViolationPoints = pointRepository.sumTotalPointsByType(PointRecordType.VIOLATION);
        int netPoints = totalRewardPoints - totalViolationPoints;

        List<MemberLeaderboardResponse> leaderboard = new ArrayList<>();
        List<Member> activeMembers = memberRepository.findByActiveTrue();
        
        for (Member member : activeMembers) {
            int memberReward = pointRepository.sumPointsByMemberIdAndType(member.getId(), PointRecordType.REWARD);
            int memberViolation = pointRepository.sumPointsByMemberIdAndType(member.getId(), PointRecordType.VIOLATION);
            if (memberReward > 0 || memberViolation > 0) {
                leaderboard.add(new MemberLeaderboardResponse(
                        member.getId(),
                        member.getFullName(),
                        memberReward,
                        memberViolation,
                        memberReward - memberViolation
                ));
            }
        }
        
        leaderboard.sort((a, b) -> Integer.compare(b.netPoints(), a.netPoints())); // descending

        return new ReportSummaryResponse(
                totalSchedules, publishedSchedules, completedSchedules,
                pendingAttendances, presentCount, absentCount, excusedCount,
                totalRewardPoints, totalViolationPoints, netPoints,
                leaderboard
        );
    }
}

