package com.pangeranvalerensco.orchestria.finance_service.controller;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import com.pangeranvalerensco.orchestria.finance_service.payload.request.CreateFundDisbursementRequest;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.FundDisbursementResponse;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.finance_service.service.FundDisbursementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/finance/disbursements")
@RequiredArgsConstructor
public class FundDisbursementController {

        private final FundDisbursementService fundDisbursementService;

        @PreAuthorize("hasAuthority('finance.disburse')")
        @PostMapping
        public ResponseEntity<ApiResponse<FundDisbursementResponse>> create(
                        @Valid @RequestBody CreateFundDisbursementRequest request,
                        Authentication authentication,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                String currentUserEmail = authentication.getName();

                FundDisbursementResponse response = fundDisbursementService.create(
                                request,
                                currentUserEmail,
                                authorizationHeader);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.<FundDisbursementResponse>builder()
                                                .success(true)
                                                .message("Pencairan dana berhasil dicatat")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAnyAuthority('finance.report.read', 'finance.disburse')")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<FundDisbursementResponse>>> getAll(
                        @RequestParam(required = false) DisbursementStatus status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "disbursedAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {
                PageResponse<FundDisbursementResponse> response = fundDisbursementService.getAll(
                                status,
                                page,
                                size,
                                sortBy,
                                sortDirection);

                return ResponseEntity.ok(
                                ApiResponse.<PageResponse<FundDisbursementResponse>>builder()
                                                .success(true)
                                                .message("Data pencairan dana berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAnyAuthority('finance.report.read', 'finance.disburse')")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<FundDisbursementResponse>> getById(@PathVariable Long id) {
                FundDisbursementResponse response = fundDisbursementService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.<FundDisbursementResponse>builder()
                                                .success(true)
                                                .message("Detail pencairan dana berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAnyAuthority('finance.report.read', 'finance.disburse')")
        @GetMapping("/by-request/{fundRequestId}")
        public ResponseEntity<ApiResponse<FundDisbursementResponse>> getByFundRequestId(
                        @PathVariable Long fundRequestId) {
                FundDisbursementResponse response = fundDisbursementService.getByFundRequestId(fundRequestId);

                return ResponseEntity.ok(
                                ApiResponse.<FundDisbursementResponse>builder()
                                                .success(true)
                                                .message("Data pencairan berdasarkan pengajuan berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('finance.disburse')")
        @PostMapping("/{id}/retry-request-sync")
        public ResponseEntity<ApiResponse<FundDisbursementResponse>> retryRequestSync(
                        @PathVariable Long id,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                FundDisbursementResponse response = fundDisbursementService.retryRequestSync(
                                id,
                                authorizationHeader);

                return ResponseEntity.ok(
                                ApiResponse.<FundDisbursementResponse>builder()
                                                .success(true)
                                                .message(
                                                                "Sinkronisasi status pengajuan berhasil")
                                                .data(response)
                                                .build());
        }
}