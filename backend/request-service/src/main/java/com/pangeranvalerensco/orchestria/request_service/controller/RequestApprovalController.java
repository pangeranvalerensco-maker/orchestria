package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.payload.request.ProcessApprovalRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.service.RequestApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/requests/{requestId}/approvals")
@RequiredArgsConstructor
public class RequestApprovalController {

    private final RequestApprovalService requestApprovalService;

    @PreAuthorize("(#request.level.name() == 'DIVISION' and hasAuthority('request.approve.division')) or (#request.level.name() == 'PUB' and hasAuthority('request.approve.pub')) or (#request.level.name() == 'PEMBINA' and hasAuthority('request.approve.pembina'))")
    @PostMapping("/approve")
    public ResponseEntity<ApiResponse<FundRequestResponse>> approve(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessApprovalRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();

        FundRequestResponse response = requestApprovalService.approve(
                requestId,
                request,
                currentUserEmail
        );

        return ResponseEntity.ok(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Pengajuan dana berhasil disetujui")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("(#request.level.name() == 'DIVISION' and hasAuthority('request.approve.division')) or (#request.level.name() == 'PUB' and hasAuthority('request.approve.pub')) or (#request.level.name() == 'PEMBINA' and hasAuthority('request.approve.pembina'))")
    @PostMapping("/reject")
    public ResponseEntity<ApiResponse<FundRequestResponse>> reject(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessApprovalRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();

        FundRequestResponse response = requestApprovalService.reject(
                requestId,
                request,
                currentUserEmail
        );

        return ResponseEntity.ok(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Pengajuan dana berhasil ditolak")
                        .data(response)
                        .build()
        );
    }

    @PreAuthorize("(#request.level.name() == 'DIVISION' and hasAuthority('request.approve.division')) or (#request.level.name() == 'PUB' and hasAuthority('request.approve.pub')) or (#request.level.name() == 'PEMBINA' and hasAuthority('request.approve.pembina'))")
    @PostMapping("/revision")
    public ResponseEntity<ApiResponse<FundRequestResponse>> requestRevision(
            @PathVariable Long requestId,
            @Valid @RequestBody ProcessApprovalRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();

        FundRequestResponse response = requestApprovalService.requestRevision(
                requestId,
                request,
                currentUserEmail
        );

        return ResponseEntity.ok(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Pengajuan dana berhasil diminta revisi")
                        .data(response)
                        .build()
        );
    }
}