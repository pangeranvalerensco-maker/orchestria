package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.CleanlinessAssignment;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleanlinessAssignmentRepository extends JpaRepository<CleanlinessAssignment, String> {
    List<CleanlinessAssignment> findByScheduleIdAndActiveTrue(String scheduleId);
    List<CleanlinessAssignment> findByMemberIdAndActiveTrue(Long memberId);
    Optional<CleanlinessAssignment> findByIdAndActiveTrue(String id);
    Optional<CleanlinessAssignment> findByScheduleIdAndMemberIdAndActiveTrue(String scheduleId, Long memberId);

    @Query("SELECT a FROM CleanlinessAssignment a WHERE a.memberId = :memberId AND a.schedule.status = 'PUBLISHED' AND a.active = true AND a.schedule.active = true ORDER BY a.schedule.dutyDate DESC")
    List<CleanlinessAssignment> findPublishedAssignmentsByMemberId(@Param("memberId") Long memberId);

    long countByAttendanceStatusAndActiveTrue(AttendanceStatus attendanceStatus);
    long countByScheduleIdAndActiveTrue(String scheduleId);
    long countByScheduleIdAndAttendanceStatusAndActiveTrue(String scheduleId, AttendanceStatus attendanceStatus);
}
