package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateRequestItemRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.service.RequestItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/requests/{requestId}/items")
@RequiredArgsConstructor
public class RequestItemController {

    private final RequestItemService requestItemService;

    @PreAuthorize("hasAuthority('request.create')")
    @PostMapping
    public ResponseEntity<ApiResponse<FundRequestResponse>> addItem(
            @PathVariable Long requestId,
            @Valid @RequestBody CreateRequestItemRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();

        FundRequestResponse response = requestItemService.addItem(
                requestId,
                request,
                currentUserEmail
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Item pengajuan berhasil ditambahkan")
                        .data(response)
                        .build()
        );
    }
}