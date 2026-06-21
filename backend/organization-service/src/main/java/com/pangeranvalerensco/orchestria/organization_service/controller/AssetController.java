package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetConditionUpdateRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.AssetResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.ConditionHistoryResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<Page<AssetResponse>>> getAllAssets(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) AssetStatus status,
            @RequestParam(required = false) AssetCondition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.<Page<AssetResponse>>builder()
                .success(true)
                .message("Berhasil mengambil data aset")
                .data(assetService.getAllAssets(search, status, condition, pageable))
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.read')")
    public ResponseEntity<ApiResponse<AssetResponse>> getAssetById(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<AssetResponse>builder()
                .success(true)
                .message("Berhasil mengambil detail aset")
                .data(assetService.getAssetById(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<ApiResponse<AssetResponse>> createAsset(@Valid @RequestBody AssetRequest request) {
        return new ResponseEntity<>(ApiResponse.<AssetResponse>builder()
                .success(true)
                .message("Berhasil membuat aset baru")
                .data(assetService.createAsset(request))
                .build(), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<ApiResponse<AssetResponse>> updateAsset(@PathVariable String id, @Valid @RequestBody AssetRequest request) {
        return ResponseEntity.ok(ApiResponse.<AssetResponse>builder()
                .success(true)
                .message("Berhasil memperbarui aset")
                .data(assetService.updateAsset(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('asset.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable String id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Berhasil menghapus aset")
                .build());
    }

    @PatchMapping("/{id}/condition")
    @PreAuthorize("hasAuthority('asset.condition.manage')")
    public ResponseEntity<ApiResponse<AssetResponse>> updateCondition(@PathVariable String id, @Valid @RequestBody AssetConditionUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.<AssetResponse>builder()
                .success(true)
                .message("Berhasil memperbarui kondisi aset")
                .data(assetService.updateCondition(id, request))
                .build());
    }

    @GetMapping("/{id}/condition-histories")
    @PreAuthorize("hasAuthority('asset.read')")
    public ResponseEntity<ApiResponse<List<ConditionHistoryResponse>>> getConditionHistories(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<List<ConditionHistoryResponse>>builder()
                .success(true)
                .message("Berhasil mengambil histori kondisi")
                .data(assetService.getConditionHistories(id))
                .build());
    }
}
