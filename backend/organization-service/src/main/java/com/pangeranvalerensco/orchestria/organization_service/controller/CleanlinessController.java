package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.AttendanceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.PointRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness.ScheduleRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.cleanliness.CleanlinessResponses.*;
import com.pangeranvalerensco.orchestria.organization_service.service.CleanlinessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/cleanliness")
@RequiredArgsConstructor
public class CleanlinessController {

    private final CleanlinessService cleanlinessService;

    // ==========================================
    // SCHEDULES
    // ==========================================

    @PostMapping("/schedules")
    @PreAuthorize("hasAuthority('cleanliness.schedule.manage')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(@Valid @RequestBody ScheduleRequest request) {
        ScheduleResponse response = cleanlinessService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ScheduleResponse>builder().success(true).message("Jadwal piket berhasil dibuat.").data(response).build());
    }

    @GetMapping("/schedules")
    @PreAuthorize("hasAuthority('cleanliness.schedule.read') or hasAuthority('cleanliness.schedule.manage') or hasAuthority('cleanliness.attendance.read') or hasAuthority('cleanliness.report.read') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getAllSchedules() {
        return ResponseEntity.ok(ApiResponse.<List<ScheduleResponse>>builder().success(true).data(cleanlinessService.getAllSchedules()).build());
    }

    @GetMapping("/schedules/my")
    @PreAuthorize("hasAuthority('cleanliness.schedule.read')")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getMySchedules() {
        return ResponseEntity.ok(ApiResponse.<List<ScheduleResponse>>builder().success(true).data(cleanlinessService.getMySchedules()).build());
    }

    @GetMapping("/schedules/{id}")
    @PreAuthorize("hasAuthority('cleanliness.schedule.read') or hasAuthority('cleanliness.schedule.manage') or hasAuthority('cleanliness.attendance.read') or hasAuthority('cleanliness.report.read') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> getSchedule(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<ScheduleResponse>builder().success(true).data(cleanlinessService.getSchedule(id)).build());
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasAuthority('cleanliness.schedule.manage')")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(@PathVariable String id, @Valid @RequestBody ScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.<ScheduleResponse>builder().success(true).message("Jadwal piket berhasil diperbarui.").data(cleanlinessService.updateSchedule(id, request)).build());
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasAuthority('cleanliness.schedule.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable String id) {
        cleanlinessService.deleteSchedule(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Jadwal piket berhasil dihapus/dibatalkan.").build());
    }

    // ==========================================
    // ATTENDANCE
    // ==========================================

    @PostMapping("/assignments/{assignmentId}/attendance")
    @PreAuthorize("hasAuthority('cleanliness.attendance.create')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> recordAttendance(
            @PathVariable String assignmentId,
            @Valid @RequestBody AttendanceRequest request) {
        AssignmentResponse response = cleanlinessService.recordAttendance(assignmentId, request);
        return ResponseEntity.ok(ApiResponse.<AssignmentResponse>builder().success(true).message("Presensi berhasil dicatat.").data(response).build());
    }

    @GetMapping("/attendances")
    @PreAuthorize("hasAuthority('cleanliness.attendance.read')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getAllAttendances(
            @RequestParam(required = false) String scheduleId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) AttendanceStatus status) {
        return ResponseEntity.ok(ApiResponse.<List<AssignmentResponse>>builder().success(true).data(cleanlinessService.getAllAttendances(scheduleId, memberId, status)).build());
    }

    // ==========================================
    // POINTS
    // ==========================================

    @PostMapping("/points")
    public ResponseEntity<ApiResponse<PointRecordResponse>> createPointRecord(@Valid @RequestBody PointRequest request) {
        // Method level preauthorize is complex due to type. Handle in service or manually check here:
        if (request.type() == com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType.REWARD) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("cleanliness.point.manage") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki akses untuk memberikan reward");
            }
        } else {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("cleanliness.violation.manage") || a.getAuthority().equals("ROLE_SUPER_ADMIN"))) {
                throw new org.springframework.security.access.AccessDeniedException("Anda tidak memiliki akses untuk memberikan violation");
            }
        }

        PointRecordResponse response = cleanlinessService.createPointRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PointRecordResponse>builder().success(true).message("Poin berhasil ditambahkan.").data(response).build());
    }

    @GetMapping("/points")
    @PreAuthorize("hasAuthority('cleanliness.report.read') or hasAuthority('cleanliness.point.manage')")
    public ResponseEntity<ApiResponse<List<PointRecordResponse>>> getAllPoints() {
        return ResponseEntity.ok(ApiResponse.<List<PointRecordResponse>>builder().success(true).data(cleanlinessService.getAllPointRecords()).build());
    }

    @GetMapping("/points/my")
    @PreAuthorize("hasAuthority('cleanliness.schedule.read')")
    public ResponseEntity<ApiResponse<List<PointRecordResponse>>> getMyPoints() {
        return ResponseEntity.ok(ApiResponse.<List<PointRecordResponse>>builder().success(true).data(cleanlinessService.getMyPointRecords()).build());
    }

    // ==========================================
    // REPORTS
    // ==========================================

    @GetMapping("/reports/summary")
    @PreAuthorize("hasAuthority('cleanliness.report.read')")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getReportSummary() {
        return ResponseEntity.ok(ApiResponse.<ReportSummaryResponse>builder().success(true).data(cleanlinessService.getReportSummary()).build());
    }
}
