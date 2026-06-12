package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateFundRequestRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.request_service.service.FundRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class FundRequestController {

    private final FundRequestService fundRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<FundRequestResponse>> create(
            @Valid @RequestBody CreateFundRequestRequest request,
            Authentication authentication
    ) {
        String currentUserEmail = authentication.getName();

        FundRequestResponse response = fundRequestService.create(request, currentUserEmail);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Pengajuan dana berhasil dibuat")
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FundRequestResponse>>> getAll(
            @RequestParam(required = false) FundRequestStatus status,
            @RequestParam(required = false) Long divisionId,
            @RequestParam(required = false) Long requesterMemberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        PageResponse<FundRequestResponse> response = fundRequestService.getAll(
                status,
                divisionId,
                requesterMemberId,
                page,
                size,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<FundRequestResponse>>builder()
                        .success(true)
                        .message("Data pengajuan dana berhasil diambil")
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FundRequestResponse>> getById(@PathVariable Long id) {
        FundRequestResponse response = fundRequestService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.<FundRequestResponse>builder()
                        .success(true)
                        .message("Detail pengajuan dana berhasil diambil")
                        .data(response)
                        .build()
        );
    }
}