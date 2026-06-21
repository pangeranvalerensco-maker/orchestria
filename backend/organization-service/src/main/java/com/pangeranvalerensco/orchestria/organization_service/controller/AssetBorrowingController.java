package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.*;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.BorrowingResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.AssetBorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organization/asset-borrowings")
@RequiredArgsConstructor
public class AssetBorrowingController {

    private final AssetBorrowingService assetBorrowingService;

    @PostMapping
    @PreAuthorize("hasAuthority('asset.borrow.create')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> createBorrowing(@Valid @RequestBody BorrowingCreateRequest request) {
        return new ResponseEntity<>(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Berhasil membuat permohonan pinjaman")
                .data(assetBorrowingService.createBorrowing(request))
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('asset.borrow.read.own')")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getMyBorrowings(
            @RequestParam(required = false) BorrowingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<BorrowingResponse>>builder()
                .success(true)
                .message("Berhasil mengambil data peminjaman saya")
                .data(assetBorrowingService.getMyBorrowings(status, pageable))
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('asset.borrow.read.all')")
    public ResponseEntity<ApiResponse<Page<BorrowingResponse>>> getAllBorrowings(
            @RequestParam(required = false) BorrowingStatus status,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String borrowerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<BorrowingResponse>>builder()
                .success(true)
                .message("Berhasil mengambil semua data peminjaman")
                .data(assetBorrowingService.getAllBorrowings(status, assetId, borrowerName, pageable))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.borrow.read.own') or hasAuthority('asset.borrow.read.all')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> getBorrowingById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Berhasil mengambil detail peminjaman")
                .data(assetBorrowingService.getBorrowingById(id))
                .build());
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> approveBorrowing(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Peminjaman disetujui")
                .data(assetBorrowingService.approveBorrowing(id))
                .build());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> rejectBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Peminjaman ditolak")
                .data(assetBorrowingService.rejectBorrowing(id, request))
                .build());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('asset.borrow.create')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> cancelBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Peminjaman dibatalkan")
                .data(assetBorrowingService.cancelBorrowing(id, request))
                .build());
    }

    @PostMapping("/{id}/cancel-approved")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> cancelApprovedBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Peminjaman yang disetujui telah dibatalkan")
                .data(assetBorrowingService.cancelApprovedBorrowing(id, request))
                .build());
    }

    @PostMapping("/{id}/handover")
    @PreAuthorize("hasAuthority('asset.borrow.handover')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> handover(@PathVariable String id, @Valid @RequestBody AssetHandoverRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Serah terima aset berhasil")
                .data(assetBorrowingService.handover(id, request))
                .build());
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('asset.borrow.create')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> requestReturn(@PathVariable String id, @Valid @RequestBody AssetReturnRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Permohonan pengembalian diajukan")
                .data(assetBorrowingService.requestReturn(id, request))
                .build());
    }

    @PostMapping("/{id}/verify-return")
    @PreAuthorize("hasAuthority('asset.return.verify')")
    public ResponseEntity<ApiResponse<BorrowingResponse>> verifyReturn(@PathVariable String id, @Valid @RequestBody AssetReturnVerificationRequest request) {
        return ResponseEntity.ok(ApiResponse.<BorrowingResponse>builder()
                .success(true)
                .message("Pengembalian diverifikasi")
                .data(assetBorrowingService.verifyReturn(id, request))
                .build());
    }
}
