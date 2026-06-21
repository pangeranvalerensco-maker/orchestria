package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishActivityRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.english.EnglishDepositVerificationRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.english.EnglishResponses.*;
import com.pangeranvalerensco.orchestria.organization_service.service.EnglishService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/english")
@RequiredArgsConstructor
public class EnglishController {

    private final EnglishService englishService;

    // ==========================================
    // ACTIVITY
    // ==========================================

    @PostMapping("/activities")
    @PreAuthorize("hasAuthority('english.activity.manage')")
    public ResponseEntity<ApiResponse<ActivityResponse>> createActivity(@Valid @RequestBody EnglishActivityRequest request) {
        ActivityResponse response = englishService.createActivity(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ActivityResponse>builder().success(true).message("Jadwal aktivitas berhasil dibuat.").data(response).build());
    }

    @GetMapping("/activities")
    @PreAuthorize("hasAuthority('english.activity.read') or hasAuthority('english.activity.manage') or hasAuthority('english.deposit.read.all') or hasAuthority('english.report.read') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getAllActivities() {
        return ResponseEntity.ok(ApiResponse.<List<ActivityResponse>>builder().success(true).data(englishService.getAllActivities()).build());
    }

    @GetMapping("/activities/{id}")
    @PreAuthorize("hasAuthority('english.activity.read') or hasAuthority('english.activity.manage') or hasAuthority('english.deposit.read.all') or hasAuthority('english.report.read') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<ActivityResponse>builder().success(true).data(englishService.getActivity(id)).build());
    }

    @PutMapping("/activities/{id}")
    @PreAuthorize("hasAuthority('english.activity.manage')")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(@PathVariable String id, @Valid @RequestBody EnglishActivityRequest request) {
        return ResponseEntity.ok(ApiResponse.<ActivityResponse>builder().success(true).message("Jadwal aktivitas berhasil diperbarui.").data(englishService.updateActivity(id, request)).build());
    }

    @DeleteMapping("/activities/{id}")
    @PreAuthorize("hasAuthority('english.activity.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(@PathVariable String id) {
        englishService.deleteActivity(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Jadwal aktivitas berhasil dihapus.").build());
    }

    // ==========================================
    // DEPOSIT
    // ==========================================

    @PostMapping("/deposits")
    @PreAuthorize("hasAuthority('english.deposit.create')")
    public ResponseEntity<ApiResponse<DepositResponse>> createDeposit(@Valid @RequestBody EnglishDepositRequest request) {
        DepositResponse response = englishService.createDeposit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DepositResponse>builder().success(true).message("Setoran berhasil dibuat.").data(response).build());
    }

    @GetMapping("/deposits")
    @PreAuthorize("hasAuthority('english.deposit.read.all')")
    public ResponseEntity<ApiResponse<List<DepositResponse>>> getAllDeposits() {
        return ResponseEntity.ok(ApiResponse.<List<DepositResponse>>builder().success(true).data(englishService.getAllDeposits()).build());
    }

    @GetMapping("/deposits/my")
    @PreAuthorize("hasAuthority('english.deposit.read.own')")
    public ResponseEntity<ApiResponse<List<DepositResponse>>> getMyDeposits() {
        return ResponseEntity.ok(ApiResponse.<List<DepositResponse>>builder().success(true).data(englishService.getMyDeposits()).build());
    }

    @GetMapping("/deposits/{id}")
    @PreAuthorize("hasAuthority('english.deposit.read.all') or hasAuthority('english.deposit.read.own') or hasAuthority('ROLE_SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DepositResponse>> getDeposit(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<DepositResponse>builder().success(true).data(englishService.getDeposit(id)).build());
    }

    @PostMapping("/deposits/{id}/verify")
    @PreAuthorize("hasAuthority('english.deposit.verify')")
    public ResponseEntity<ApiResponse<DepositResponse>> verifyDeposit(@PathVariable String id, @Valid @RequestBody EnglishDepositVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.<DepositResponse>builder().success(true).message("Setoran berhasil diverifikasi.").data(englishService.verifyDeposit(id, request)).build());
    }

    // ==========================================
    // REPORT
    // ==========================================

    @GetMapping("/reports/summary")
    @PreAuthorize("hasAuthority('english.report.read')")
    public ResponseEntity<ApiResponse<ReportSummaryResponse>> getReportSummary() {
        return ResponseEntity.ok(ApiResponse.<ReportSummaryResponse>builder().success(true).data(englishService.getReportSummary()).build());
    }
}
