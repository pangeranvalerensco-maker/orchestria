package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.payload.request.RequestSettlementRevisionRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;
import com.pangeranvalerensco.orchestria.request_service.service.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/{requestId}/settlement")
    @PreAuthorize("hasAnyAuthority('request.read.own', 'finance.settlement.verify')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> getSettlement(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            Authentication authentication) {
        boolean canVerifySettlement = authentication.getAuthorities().stream()
                .anyMatch(authority -> "finance.settlement.verify"
                        .equals(authority.getAuthority()));

        RequestSettlementResponse response = settlementService.getSettlement(
                requestId,
                currentUser,
                canVerifySettlement);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Detail settlement berhasil diambil")
                        .data(response)
                        .build());
    }

    @PostMapping("/{requestId}/settlement")
    @PreAuthorize("hasAuthority('request.create')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> submitFirst(
            @PathVariable Long requestId,
            @Valid @RequestBody SubmitSettlementRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        RequestSettlementResponse response = settlementService.submitFirst(
                requestId,
                request,
                currentUser);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Settlement berhasil dikirim")
                        .data(response)
                        .build());
    }

    @PutMapping("/{requestId}/settlement")
    @PreAuthorize("hasAuthority('request.create')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> resubmit(
            @PathVariable Long requestId,
            @Valid @RequestBody SubmitSettlementRequest request,
            @AuthenticationPrincipal AuthenticatedUser currentUser) {
        RequestSettlementResponse response = settlementService.resubmit(
                requestId,
                request,
                currentUser);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Settlement berhasil diperbaiki dan dikirim ulang")
                        .data(response)
                        .build());
    }

    @PostMapping("/{requestId}/settlement/request-revision")
    @PreAuthorize("hasAuthority('finance.settlement.verify')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> requestRevision(
            @PathVariable Long requestId,
            @Valid @RequestBody RequestSettlementRevisionRequest request,
            @AuthenticationPrincipal AuthenticatedUser reviewer) {
        RequestSettlementResponse response = settlementService.requestRevision(
                requestId,
                request,
                reviewer);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Revisi settlement berhasil diminta")
                        .data(response)
                        .build());
    }

    @PostMapping("/{requestId}/settlement/approve")
    @PreAuthorize("hasAuthority('finance.settlement.verify')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal AuthenticatedUser reviewer) {
        RequestSettlementResponse response = settlementService.approve(
                requestId,
                reviewer);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Settlement berhasil disetujui")
                        .data(response)
                        .build());
    }
}
