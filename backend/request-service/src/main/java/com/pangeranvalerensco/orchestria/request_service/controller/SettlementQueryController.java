package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.service.SettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class SettlementQueryController {

    private final SettlementQueryService settlementQueryService;

    @GetMapping("/{id}/settlement")
    @PreAuthorize("hasAuthority('finance.settlement.verify')")
    public ResponseEntity<ApiResponse<RequestSettlementResponse>> getSettlement(
            @PathVariable Long id) {
        RequestSettlementResponse response = settlementQueryService
                .getByFundRequestId(id);

        return ResponseEntity.ok(
                ApiResponse.<RequestSettlementResponse>builder()
                        .success(true)
                        .message("Detail laporan penggunaan dana berhasil diambil")
                        .data(response)
                        .build());
    }
}
