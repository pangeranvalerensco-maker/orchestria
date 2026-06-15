package com.pangeranvalerensco.orchestria.request_service.controller;

import com.pangeranvalerensco.orchestria.request_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestApprovalResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestStatusHistoryResponse;
import com.pangeranvalerensco.orchestria.request_service.service.RequestTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}")
@RequiredArgsConstructor
public class RequestTimelineController {

        private final RequestTimelineService requestTimelineService;

        @PreAuthorize("hasAuthority('request.read.all')")
        @GetMapping("/approvals")
        public ResponseEntity<ApiResponse<List<RequestApprovalResponse>>> getApprovals(
                        @PathVariable Long requestId) {
                List<RequestApprovalResponse> response = requestTimelineService.getApprovals(requestId);

                return ResponseEntity.ok(
                                ApiResponse.<List<RequestApprovalResponse>>builder()
                                                .success(true)
                                                .message("Data approval pengajuan berhasil diambil")
                                                .data(response)
                                                .build());
        }

        @PreAuthorize("hasAuthority('request.read.all')")
        @GetMapping("/histories")
        public ResponseEntity<ApiResponse<List<RequestStatusHistoryResponse>>> getHistories(
                        @PathVariable Long requestId) {
                List<RequestStatusHistoryResponse> response = requestTimelineService.getHistories(requestId);

                return ResponseEntity.ok(
                                ApiResponse.<List<RequestStatusHistoryResponse>>builder()
                                                .success(true)
                                                .message("Histori status pengajuan berhasil diambil")
                                                .data(response)
                                                .build());
        }
}