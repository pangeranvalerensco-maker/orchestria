package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetConditionUpdateRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.AssetResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.ConditionHistoryResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.AssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAuthority('asset.read')")
    public ResponseEntity<Page<AssetResponse>> getAllAssets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) AssetCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(assetService.getAllAssets(search, status, condition, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.read')")
    public ResponseEntity<AssetResponse> getAssetById(@PathVariable String id) {
        return ResponseEntity.ok(assetService.getAssetById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<AssetResponse> createAsset(@Valid @RequestBody AssetRequest request) {
        return new ResponseEntity<>(assetService.createAsset(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<AssetResponse> updateAsset(@PathVariable String id, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(assetService.updateAsset(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<Void> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/condition")
    @PreAuthorize("hasAuthority('asset.condition.manage')")
    public ResponseEntity<AssetResponse> updateCondition(@PathVariable String id, @Valid @RequestBody AssetConditionUpdateRequest request) {
        return ResponseEntity.ok(assetService.updateCondition(id, request));
    }

    @GetMapping("/{id}/condition-histories")
    @PreAuthorize("hasAuthority('asset.read')")
    public ResponseEntity<List<ConditionHistoryResponse>> getConditionHistories(@PathVariable String id) {
        return ResponseEntity.ok(assetService.getConditionHistories(id));
    }
}
