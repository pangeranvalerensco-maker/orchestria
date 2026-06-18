package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateFundRequestRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.service.FundRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class FundRequestController {

        private final FundRequestService fundRequestService;

        @PreAuthorize("hasAuthority('request.create')")
        @PostMapping
        public ResponseEntity<ApiResponse<FundRequestResponse>> create(
                        @Valid @RequestBody CreateFundRequestRequest request,
                        @AuthenticationPrincipal AuthenticatedUser currentUser,
                        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
                FundRequestResponse response = fundRequestService.create(
                                request,
                                currentUser,
                                authorizationHeader);

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message("Pengajuan dana berhasil dibuat")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.read.all')")
        @GetMapping
        public ResponseEntity<ApiResponse<PageResponse<FundRequestResponse>>> getAll(
                        @RequestParam(required = false) FundRequestStatus status,
                        @RequestParam(required = false) Long divisionId,
                        @RequestParam(required = false) Long requesterMemberId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {
                PageResponse<FundRequestResponse> response = fundRequestService.getAll(
                                status,
                                divisionId,
                                requesterMemberId,
                                page,
                                size,
                                sortBy,
                                sortDirection);

                return ResponseEntity.ok(
                                ApiResponse.<PageResponse<FundRequestResponse>>builder()
                                                .success(true)
                                                .message("Data pengajuan dana berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.read.own')")
        @GetMapping("/my")
        public ResponseEntity<ApiResponse<PageResponse<FundRequestResponse>>> getMyRequests(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                PageResponse<FundRequestResponse> response = fundRequestService.getMyRequests(
                                currentUserEmail,
                                page,
                                size,
                                sortBy,
                                sortDirection);

                return ResponseEntity.ok(
                                ApiResponse.<PageResponse<FundRequestResponse>>builder()
                                                .success(true)
                                                .message("Data pengajuan milik saya berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.read.own')")
        @GetMapping("/my/{id}")
        public ResponseEntity<ApiResponse<FundRequestResponse>> getMyRequestById(
                        @PathVariable Long id,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                FundRequestResponse response = fundRequestService.getMyRequestById(
                                id,
                                currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message(
                                                                "Detail pengajuan milik saya berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.read.all')")
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<FundRequestResponse>> getById(@PathVariable Long id) {
                FundRequestResponse response = fundRequestService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message("Detail pengajuan dana berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.create')")
        @PostMapping("/{id}/submit")
        public ResponseEntity<ApiResponse<FundRequestResponse>> submit(
                        @PathVariable Long id,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                FundRequestResponse response = fundRequestService.submit(id, currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message("Pengajuan dana berhasil disubmit")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('finance.disburse')")
        @PostMapping("/{id}/mark-disbursed")
        public ResponseEntity<ApiResponse<FundRequestResponse>> markDisbursed(
                        @PathVariable Long id,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                FundRequestResponse response = fundRequestService.markDisbursed(id, currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message("Pengajuan dana berhasil ditandai sudah dicairkan")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.create')")
        @PostMapping("/{id}/confirm-received")
        public ResponseEntity<ApiResponse<FundRequestResponse>> markFundReceived(
                        @PathVariable Long id,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                FundRequestResponse response = fundRequestService.markFundReceived(id, currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<FundRequestResponse>builder()
                                                .success(true)
                                                .message("Dana pengajuan berhasil dikonfirmasi diterima")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.create')")
        @PostMapping("/{id}/settlement")
        public ResponseEntity<ApiResponse<RequestSettlementResponse>> submitSettlement(
                        @PathVariable Long id,
                        @Valid @RequestBody SubmitSettlementRequest request,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                RequestSettlementResponse response = fundRequestService.submitSettlement(
                                id,
                                request,
                                currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<RequestSettlementResponse>builder()
                                                .success(true)
                                                .message("Settlement pengajuan berhasil dikirim")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('finance.settlement.verify')")
        @PostMapping("/{id}/settlement/approve")
        public ResponseEntity<ApiResponse<RequestSettlementResponse>> approveSettlement(
                        @PathVariable Long id,
                        Authentication authentication) {
                String currentUserEmail = authentication.getName();

                RequestSettlementResponse response = fundRequestService.approveSettlement(id, currentUserEmail);

                return ResponseEntity.ok(
                                ApiResponse.<RequestSettlementResponse>builder()
                                                .success(true)
                                                .message("Settlement pengajuan berhasil disetujui")
                                                .data(response)
                                                .build());
        }
}