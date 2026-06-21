package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.*;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.BorrowingResponse;
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
    public ResponseEntity<BorrowingResponse> createBorrowing(@Valid @RequestBody BorrowingCreateRequest request) {
        return new ResponseEntity<>(assetBorrowingService.createBorrowing(request), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('asset.borrow.read.own')")
    public ResponseEntity<Page<BorrowingResponse>> getMyBorrowings(
            @RequestParam(required = false) BorrowingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(assetBorrowingService.getMyBorrowings(status, pageable));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('asset.borrow.read.all')")
    public ResponseEntity<Page<BorrowingResponse>> getAllBorrowings(
            @RequestParam(required = false) BorrowingStatus status,
            @RequestParam(required = false) String assetId,
            @RequestParam(required = false) String borrowerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(assetBorrowingService.getAllBorrowings(status, assetId, borrowerName, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.borrow.read.own') or hasAuthority('asset.borrow.read.all')")
    public ResponseEntity<BorrowingResponse> getBorrowingById(@PathVariable String id) {
        return ResponseEntity.ok(assetBorrowingService.getBorrowingById(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<BorrowingResponse> approveBorrowing(@PathVariable String id) {
        return ResponseEntity.ok(assetBorrowingService.approveBorrowing(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<BorrowingResponse> rejectBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(assetBorrowingService.rejectBorrowing(id, request));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('asset.borrow.create')")
    public ResponseEntity<BorrowingResponse> cancelBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(assetBorrowingService.cancelBorrowing(id, request));
    }

    @PostMapping("/{id}/cancel-approved")
    @PreAuthorize("hasAuthority('asset.borrow.approve')")
    public ResponseEntity<BorrowingResponse> cancelApprovedBorrowing(@PathVariable String id, @Valid @RequestBody BorrowingDecisionRequest request) {
        return ResponseEntity.ok(assetBorrowingService.cancelApprovedBorrowing(id, request));
    }

    @PostMapping("/{id}/handover")
    @PreAuthorize("hasAuthority('asset.borrow.handover')")
    public ResponseEntity<BorrowingResponse> handover(@PathVariable String id, @Valid @RequestBody AssetHandoverRequest request) {
        return ResponseEntity.ok(assetBorrowingService.handover(id, request));
    }

    @PostMapping("/{id}/return")
    @PreAuthorize("hasAuthority('asset.borrow.create')")
    public ResponseEntity<BorrowingResponse> requestReturn(@PathVariable String id, @Valid @RequestBody AssetReturnRequest request) {
        return ResponseEntity.ok(assetBorrowingService.requestReturn(id, request));
    }

    @PostMapping("/{id}/verify-return")
    @PreAuthorize("hasAuthority('asset.return.verify')")
    public ResponseEntity<BorrowingResponse> verifyReturn(@PathVariable String id, @Valid @RequestBody AssetReturnVerificationRequest request) {
        return ResponseEntity.ok(assetBorrowingService.verifyReturn(id, request));
    }
}
