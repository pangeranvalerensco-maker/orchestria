package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Asset;
import com.pangeranvalerensco.orchestria.organization_service.entity.AssetBorrowing;
import com.pangeranvalerensco.orchestria.organization_service.entity.AssetConditionHistory;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetConditionUpdateRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.AssetRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.AssetResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.ConditionHistoryResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetBorrowingRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetConditionHistoryRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetBorrowingRepository assetBorrowingRepository;
    private final AssetConditionHistoryRepository assetConditionHistoryRepository;
    private final AssetAccessService assetAccessService;

    public Page<AssetResponse> getAllAssets(String search, AssetStatus status, AssetCondition condition, Pageable pageable) {
        boolean isManager = assetAccessService.isGlobalAssetManager() || hasManagePermission();
        
        Page<Asset> assets;
        if (isManager) {
            assets = assetRepository.searchAllAssets(search, status, condition, pageable);
            // manager sees inactive as well if they query explicitly, but default query we can let the repository handle active only or not. 
            // The requirement says manager sees all active, including maintenance/lost. Active=true is the baseline for regular catalog, but let's check requirement:
            // "Asset detail manager boleh melihat semua asset aktif, termasuk maintenance/lost."
            // "asset inactive tidak tampil pada katalog anggota;"
            // If they are not manager, they only see active assets. Actually, inactive assets shouldn't be seen by anyone in catalog unless explicitly fetched.
            // But let's assume searchActiveAssets already filters active=true. Manager can use searchAllAssets.
        } else {
            assets = assetRepository.searchActiveAssets(search, status, condition, pageable);
        }

        return assets.map(this::mapToResponse);
    }

    public AssetResponse getAssetById(String id) {
        Asset asset = assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        if (!asset.isActive() && !hasManagePermission()) {
            throw new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan.");
        }

        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse createAsset(AssetRequest request) {
        String code = request.assetCode().trim().toUpperCase();

        if (assetRepository.existsByAssetCodeIgnoreCase(code)) {
            throw new BadRequestException("Asset code sudah digunakan.");
        }

        Asset asset = Asset.builder()
                .assetCode(code)
                .assetName(request.assetName())
                .category(request.category())
                .description(request.description())
                .currentStatus(AssetStatus.AVAILABLE)
                .currentCondition(request.currentCondition() != null ? request.currentCondition() : AssetCondition.UNKNOWN)
                .location(request.location())
                .responsibleMemberId(request.responsibleMemberId())
                .imageUrl(validateAndCleanUrl(request.imageUrl()))
                .active(true)
                .build();

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    @Transactional
    public AssetResponse updateAsset(String id, AssetRequest request) {
        Asset asset = assetRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        String code = request.assetCode().trim().toUpperCase();

        if (!asset.getAssetCode().equalsIgnoreCase(code) && assetRepository.existsByAssetCodeIgnoreCase(code)) {
            throw new BadRequestException("Asset code sudah digunakan.");
        }

        asset.setAssetCode(code);
        asset.setAssetName(request.assetName());
        asset.setCategory(request.category());
        asset.setDescription(request.description());
        asset.setLocation(request.location());
        asset.setResponsibleMemberId(request.responsibleMemberId());
        asset.setImageUrl(validateAndCleanUrl(request.imageUrl()));
        
        // currentStatus and currentCondition are NOT updated via general metadata update

        asset = assetRepository.save(asset);
        return mapToResponse(asset);
    }

    @Transactional
    public void deleteAsset(String id) {
        Asset asset = assetRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        boolean hasOpenBorrowing = !assetBorrowingRepository.findByAssetIdAndStatusInAndActiveTrue(
                asset.getId(),
                List.of(BorrowingStatus.REQUESTED, BorrowingStatus.APPROVED, BorrowingStatus.BORROWED, BorrowingStatus.RETURN_REQUESTED)
        ).isEmpty();

        if (hasOpenBorrowing) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        asset.setActive(false);
        asset.setCurrentStatus(AssetStatus.INACTIVE);
        assetRepository.save(asset);
    }

    @Transactional
    public AssetResponse updateCondition(String id, AssetConditionUpdateRequest request) {
        Asset asset = assetRepository.findByIdAndActiveTrueWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        AssetStatus newStatus = request.newStatus();
        AssetCondition newCondition = request.newCondition();

        if (newStatus == AssetStatus.BORROWED || newStatus == AssetStatus.RESERVED) {
            throw new BadRequestException("Status BORROWED/RESERVED tidak dapat diatur secara manual.");
        }

        if ((asset.getCurrentStatus() == AssetStatus.BORROWED || asset.getCurrentStatus() == AssetStatus.RESERVED) && newStatus == AssetStatus.AVAILABLE) {
            throw new BadRequestException("Aset sedang dipinjam atau direservasi, tidak dapat diubah menjadi AVAILABLE.");
        }

        if ((newStatus == AssetStatus.MAINTENANCE || newStatus == AssetStatus.LOST) && (request.note() == null || request.note().isBlank())) {
            throw new BadRequestException("Perubahan ke MAINTENANCE/LOST harus menyertakan catatan (note).");
        }

        AssetCondition oldCondition = asset.getCurrentCondition();

        asset.setCurrentStatus(newStatus);
        asset.setCurrentCondition(newCondition);

        if (newStatus == AssetStatus.INACTIVE) {
            boolean hasOpenBorrowing = !assetBorrowingRepository.findByAssetIdAndStatusInAndActiveTrue(
                    asset.getId(),
                    List.of(BorrowingStatus.REQUESTED, BorrowingStatus.APPROVED, BorrowingStatus.BORROWED, BorrowingStatus.RETURN_REQUESTED)
            ).isEmpty();

            if (hasOpenBorrowing) {
                throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
            }
            asset.setActive(false);
        }

        asset = assetRepository.save(asset);

        // create condition history only if condition changed or status changed
        if (oldCondition != newCondition || request.newStatus() != asset.getCurrentStatus() || request.note() != null) {
            AssetConditionHistory history = AssetConditionHistory.builder()
                    .asset(asset)
                    .oldCondition(oldCondition)
                    .newCondition(newCondition)
                    .checkedByEmail(assetAccessService.getCurrentEmail())
                    .note(request.note())
                    .checkedAt(LocalDateTime.now())
                    .build();
            assetConditionHistoryRepository.save(history);
        }

        return mapToResponse(asset);
    }

    public List<ConditionHistoryResponse> getConditionHistories(String id) {
        if (!assetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan.");
        }

        return assetConditionHistoryRepository.findByAssetIdOrderByCreatedAtDesc(id).stream()
                .map(this::mapHistoryToResponse)
                .collect(Collectors.toList());
    }

    public AssetResponse mapToResponse(Asset asset) {
        boolean available = asset.getCurrentStatus() == AssetStatus.AVAILABLE && asset.isActive();
        
        String activeBorrowingId = null;
        if (asset.isActive()) {
             List<AssetBorrowing> openBorrowings = assetBorrowingRepository.findByAssetIdAndStatusInAndActiveTrue(
                    asset.getId(),
                    List.of(BorrowingStatus.REQUESTED, BorrowingStatus.APPROVED, BorrowingStatus.BORROWED, BorrowingStatus.RETURN_REQUESTED)
            );
             if (!openBorrowings.isEmpty()) {
                 activeBorrowingId = openBorrowings.get(0).getId();
             }
        }

        return new AssetResponse(
                asset.getId(),
                asset.getAssetCode(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getDescription(),
                asset.getCurrentStatus(),
                asset.getCurrentCondition(),
                asset.getLocation(),
                asset.getResponsibleMemberId(),
                asset.getImageUrl(),
                asset.isActive(),
                available,
                activeBorrowingId,
                asset.getCreatedAt(),
                asset.getUpdatedAt()
        );
    }

    private ConditionHistoryResponse mapHistoryToResponse(AssetConditionHistory history) {
        return new ConditionHistoryResponse(
                history.getId(),
                history.getAsset().getId(),
                history.getBorrowing() != null ? history.getBorrowing().getId() : null,
                history.getOldCondition(),
                history.getNewCondition(),
                history.getCheckedByEmail(),
                history.getNote(),
                history.getCheckedAt(),
                history.getCreatedAt()
        );
    }

    private boolean hasManagePermission() {
        try {
            assetAccessService.validateAssetOperationalPermission();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String validateAndCleanUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String cleanUrl = url.trim();
        String lowerUrl = cleanUrl.toLowerCase(java.util.Locale.ROOT);
        
        if (lowerUrl.startsWith("javascript:") || lowerUrl.startsWith("data:") || lowerUrl.startsWith("vbscript:")) {
            throw new BadRequestException("Skema URL tidak diizinkan.");
        }
        
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://") && !lowerUrl.startsWith("/")) {
            throw new BadRequestException("URL harus berupa http/https atau path relatif absolut.");
        }
        
        return cleanUrl;
    }
}
