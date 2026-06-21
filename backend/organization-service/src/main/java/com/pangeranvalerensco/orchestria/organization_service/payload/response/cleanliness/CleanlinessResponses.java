package com.pangeranvalerensco.orchestria.organization_service.payload.response.cleanliness;

import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessPointRecord;
import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessSchedule;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.ScheduleStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class CleanlinessResponses {

    public record ScheduleResponse(
            String id,
            String title,
            LocalDate dutyDate,
            LocalTime startTime,
            LocalTime endTime,
            String location,
            String description,
            ScheduleStatus status,
            LocalDateTime createdAt,
            List<AssignmentResponse> assignments
    ) {
        public static ScheduleResponse fromEntity(CleanlinessSchedule schedule, List<CleanlinessAssignment> assignments) {
            return new ScheduleResponse(
                    schedule.getId(),
                    schedule.getTitle(),
                    schedule.getDutyDate(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getLocation(),
                    schedule.getDescription(),
                    schedule.getStatus(),
                    schedule.getCreatedAt(),
                    assignments != null ? assignments.stream().map(AssignmentResponse::fromEntity).toList() : List.of()
            );
        }
    }

    public record AssignmentResponse(
            String id,
            String scheduleId,
            Long memberId,
            String memberName,
            AttendanceStatus attendanceStatus,
            String attendanceNote,
            String evidenceUrl,
            LocalDateTime attendedAt
    ) {
        public static AssignmentResponse fromEntity(CleanlinessAssignment assignment) {
            return new AssignmentResponse(
                    assignment.getId(),
                    assignment.getSchedule().getId(),
                    assignment.getMemberId(),
                    assignment.getMemberName(),
                    assignment.getAttendanceStatus(),
                    assignment.getAttendanceNote(),
                    assignment.getEvidenceUrl(),
                    assignment.getAttendedAt()
            );
        }
    }

    public record PointRecordResponse(
            String id,
            Long memberId,
            String memberName,
            String scheduleId,
            PointRecordType type,
            int pointValue,
            String reason,
            LocalDateTime recordedAt
    ) {
        public static PointRecordResponse fromEntity(CleanlinessPointRecord record) {
            return new PointRecordResponse(
                    record.getId(),
                    record.getMemberId(),
                    record.getMemberName(),
                    record.getScheduleId(),
                    record.getType(),
                    record.getPointValue(),
                    record.getReason(),
                    record.getRecordedAt()
            );
        }
    }

    public record ReportSummaryResponse(
            long totalSchedules,
            long publishedSchedules,
            long completedSchedules,
            long pendingAttendances,
            long presentCount,
            long absentCount,
            long excusedCount,
            int totalRewardPoints,
            int totalViolationPoints,
            int netPoints,
            List<MemberLeaderboardResponse> memberLeaderboard
    ) {}

    public record MemberLeaderboardResponse(
            Long memberId,
            String memberName,
            int rewardPoints,
            int violationPoints,
            int netPoints
    ) {}
}
