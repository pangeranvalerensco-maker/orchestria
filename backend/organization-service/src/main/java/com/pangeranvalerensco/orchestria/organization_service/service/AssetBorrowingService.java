package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.Asset;
import com.pangeranvalerensco.orchestria.organization_service.entity.AssetBorrowing;
import com.pangeranvalerensco.orchestria.organization_service.entity.AssetConditionHistory;
import com.pangeranvalerensco.orchestria.organization_service.entity.Member;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import com.pangeranvalerensco.orchestria.organization_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.organization_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.asset.*;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.asset.BorrowingResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetBorrowingRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetConditionHistoryRepository;
import com.pangeranvalerensco.orchestria.organization_service.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetBorrowingService {

    private final AssetBorrowingRepository assetBorrowingRepository;
    private final AssetRepository assetRepository;
    private final AssetConditionHistoryRepository assetConditionHistoryRepository;
    private final AssetAccessService assetAccessService;
    private final AssetService assetService;

    @Transactional
    public BorrowingResponse createBorrowing(BorrowingCreateRequest request) {
        Member member = assetAccessService.getCurrentMember();

        if (!member.getActive() || member.getStatus() != com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus.ACTIVE) {
            throw new BadRequestException("Anggota tidak aktif.");
        }

        Asset asset = assetRepository.findByIdAndActiveTrueWithLock(request.assetId())
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        if (asset.getCurrentStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        if (request.borrowDate().isBefore(LocalDate.now())) {
            throw new BadRequestException("Tanggal peminjaman tidak valid.");
        }

        if (request.expectedReturnDate().isBefore(request.borrowDate())) {
            throw new BadRequestException("Tanggal pengembalian tidak valid.");
        }

        List<AssetBorrowing> existingBorrowings = assetBorrowingRepository.findByAssetIdAndStatusInAndActiveTrue(
                asset.getId(),
                List.of(BorrowingStatus.REQUESTED, BorrowingStatus.APPROVED, BorrowingStatus.BORROWED, BorrowingStatus.RETURN_REQUESTED)
        );

        boolean userHasActiveRequestForThisAsset = existingBorrowings.stream()
                .anyMatch(b -> b.getBorrowerMemberId().equals(member.getId()));

        if (userHasActiveRequestForThisAsset) {
            throw new BadRequestException("Anda sudah memiliki request aktif untuk aset ini.");
        }

        AssetBorrowing borrowing = AssetBorrowing.builder()
                .asset(asset)
                .borrowerMemberId(member.getId())
                .borrowerAuthUserId(member.getAuthUserId())
                .borrowerName(member.getFullName())
                .borrowerEmail(member.getEmail())
                .purpose(request.purpose())
                .borrowDate(request.borrowDate())
                .expectedReturnDate(request.expectedReturnDate())
                .status(BorrowingStatus.REQUESTED)
                .active(true)
                .build();

        borrowing = assetBorrowingRepository.save(borrowing);
        return mapToResponse(borrowing);
    }

    public Page<BorrowingResponse> getMyBorrowings(BorrowingStatus status, Pageable pageable) {
        Member member = assetAccessService.getCurrentMember();
        return assetBorrowingRepository.findByBorrowerMemberIdAndActiveTrueOrderByCreatedAtDesc(member.getId(), status, pageable)
                .map(this::mapToResponse);
    }

    public Page<BorrowingResponse> getAllBorrowings(BorrowingStatus status, String assetId, String borrowerName, Pageable pageable) {
        return assetBorrowingRepository.findAllWithFilters(status, assetId, borrowerName, pageable)
                .map(this::mapToResponse);
    }

    public BorrowingResponse getBorrowingById(String id) {
        AssetBorrowing borrowing = assetBorrowingRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));

        assetAccessService.validateBorrowingReadAccess(borrowing);
        return mapToResponse(borrowing);
    }

    @Transactional
    public BorrowingResponse approveBorrowing(String id) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateAssetOperationalPermission();

        if (borrowing.getStatus() != BorrowingStatus.REQUESTED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        Asset asset = getLockedAsset(borrowing.getAsset().getId());
        if (asset.getCurrentStatus() != AssetStatus.AVAILABLE) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        borrowing.setStatus(BorrowingStatus.APPROVED);
        borrowing.setApprovedByEmail(assetAccessService.getCurrentEmail());
        borrowing.setApprovedAt(LocalDateTime.now());

        asset.setCurrentStatus(AssetStatus.RESERVED);

        assetRepository.save(asset);
        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse rejectBorrowing(String id, BorrowingDecisionRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateAssetOperationalPermission();

        if (borrowing.getStatus() != BorrowingStatus.REQUESTED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        borrowing.setStatus(BorrowingStatus.REJECTED);
        borrowing.setRejectionReason(request.reason());

        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse cancelBorrowing(String id, BorrowingDecisionRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateBorrowingOwner(borrowing);

        if (borrowing.getStatus() != BorrowingStatus.REQUESTED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        borrowing.setStatus(BorrowingStatus.CANCELLED);
        borrowing.setCancellationReason(request.reason());

        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse cancelApprovedBorrowing(String id, BorrowingDecisionRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateAssetOperationalPermission();

        if (borrowing.getStatus() != BorrowingStatus.APPROVED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        Asset asset = getLockedAsset(borrowing.getAsset().getId());

        borrowing.setStatus(BorrowingStatus.CANCELLED);
        borrowing.setCancellationReason(request.reason());

        asset.setCurrentStatus(AssetStatus.AVAILABLE);

        assetRepository.save(asset);
        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse handover(String id, AssetHandoverRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateAssetOperationalPermission();

        if (borrowing.getStatus() != BorrowingStatus.APPROVED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        Asset asset = getLockedAsset(borrowing.getAsset().getId());
        if (asset.getCurrentStatus() != AssetStatus.RESERVED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        String cleanedUrl = AssetService.validateAndCleanUrl(request.handoverProofUrl());

        AssetCondition oldCondition = asset.getCurrentCondition();
        AssetCondition newCondition = request.conditionBefore();

        borrowing.setStatus(BorrowingStatus.BORROWED);
        borrowing.setHandedOverByEmail(assetAccessService.getCurrentEmail());
        borrowing.setHandedOverAt(LocalDateTime.now());
        borrowing.setConditionBefore(newCondition);
        borrowing.setHandoverProofUrl(cleanedUrl);
        borrowing.setNote(request.note());

        asset.setCurrentStatus(AssetStatus.BORROWED);
        asset.setCurrentCondition(newCondition);

        assetRepository.save(asset);
        
        if (oldCondition != newCondition || AssetStatus.RESERVED != AssetStatus.BORROWED) {
            AssetConditionHistory history = AssetConditionHistory.builder()
                    .asset(asset)
                    .borrowing(borrowing)
                    .oldStatus(AssetStatus.RESERVED)
                    .newStatus(AssetStatus.BORROWED)
                    .oldCondition(oldCondition)
                    .newCondition(newCondition)
                    .checkedByEmail(assetAccessService.getCurrentEmail())
                    .note(request.note())
                    .checkedAt(LocalDateTime.now())
                    .build();
            assetConditionHistoryRepository.save(history);
        }

        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse requestReturn(String id, AssetReturnRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateBorrowingOwner(borrowing);

        if (borrowing.getStatus() != BorrowingStatus.BORROWED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        String cleanedUrl = AssetService.validateAndCleanUrl(request.returnProofUrl());

        borrowing.setStatus(BorrowingStatus.RETURN_REQUESTED);
        borrowing.setReturnRequestedAt(LocalDateTime.now());
        borrowing.setReturnProofUrl(cleanedUrl);
        
        if (request.note() != null) {
            borrowing.setNote(borrowing.getNote() == null ? request.note() : borrowing.getNote() + "\n" + request.note());
        }

        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    @Transactional
    public BorrowingResponse verifyReturn(String id, AssetReturnVerificationRequest request) {
        AssetBorrowing borrowing = getLockedBorrowing(id);
        assetAccessService.validateAssetOperationalPermission();

        if (borrowing.getStatus() != BorrowingStatus.RETURN_REQUESTED) {
            throw new BadRequestException("Aset sedang digunakan atau status peminjaman mengalami konflik.");
        }

        Asset asset = getLockedAsset(borrowing.getAsset().getId());

        AssetCondition oldCondition = asset.getCurrentCondition();
        AssetCondition newCondition = request.conditionAfter();

        borrowing.setStatus(BorrowingStatus.RETURN_VERIFIED);
        borrowing.setActualReturnDate(LocalDate.now());
        borrowing.setReturnVerifiedByEmail(assetAccessService.getCurrentEmail());
        borrowing.setReturnVerifiedAt(LocalDateTime.now());
        borrowing.setConditionAfter(newCondition);

        if (request.note() != null) {
            borrowing.setNote(borrowing.getNote() == null ? request.note() : borrowing.getNote() + "\n" + request.note());
        }

        asset.setCurrentCondition(newCondition);
        if (newCondition == AssetCondition.GOOD || newCondition == AssetCondition.MINOR_DAMAGE) {
            asset.setCurrentStatus(AssetStatus.AVAILABLE);
        } else {
            asset.setCurrentStatus(AssetStatus.MAINTENANCE);
        }

        assetRepository.save(asset);

        AssetConditionHistory history = AssetConditionHistory.builder()
                .asset(asset)
                .borrowing(borrowing)
                .oldStatus(AssetStatus.BORROWED)
                .newStatus(asset.getCurrentStatus())
                .oldCondition(oldCondition)
                .newCondition(newCondition)
                .checkedByEmail(assetAccessService.getCurrentEmail())
                .note(request.note())
                .checkedAt(LocalDateTime.now())
                .build();
        assetConditionHistoryRepository.save(history);

        return mapToResponse(assetBorrowingRepository.save(borrowing));
    }

    private AssetBorrowing getLockedBorrowing(String id) {
        return assetBorrowingRepository.findByIdAndActiveTrueWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));
    }

    private Asset getLockedAsset(String id) {
        return assetRepository.findByIdAndActiveTrueWithLock(id)
                .orElseThrow(() -> new ResourceNotFoundException("Aset atau peminjaman tidak ditemukan."));
    }

    private BorrowingResponse mapToResponse(AssetBorrowing borrowing) {
        boolean overdue = false;
        if (borrowing.getStatus() == BorrowingStatus.BORROWED || borrowing.getStatus() == BorrowingStatus.RETURN_REQUESTED) {
            if (borrowing.getExpectedReturnDate().isBefore(LocalDate.now())) {
                overdue = true;
            }
        } else if (borrowing.getStatus() == BorrowingStatus.RETURN_VERIFIED) {
            if (borrowing.getActualReturnDate() != null && borrowing.getExpectedReturnDate().isBefore(borrowing.getActualReturnDate())) {
                overdue = true;
            }
        }

        return new BorrowingResponse(
                borrowing.getId(),
                assetService.mapToResponse(borrowing.getAsset()),
                borrowing.getBorrowerMemberId(),
                borrowing.getBorrowerAuthUserId(),
                borrowing.getBorrowerName(),
                borrowing.getBorrowerEmail(),
                borrowing.getPurpose(),
                borrowing.getBorrowDate(),
                borrowing.getExpectedReturnDate(),
                borrowing.getActualReturnDate(),
                borrowing.getStatus(),
                overdue,
                borrowing.getRejectionReason(),
                borrowing.getCancellationReason(),
                borrowing.getApprovedByEmail(),
                borrowing.getApprovedAt(),
                borrowing.getHandedOverByEmail(),
                borrowing.getHandedOverAt(),
                borrowing.getReturnRequestedAt(),
                borrowing.getReturnVerifiedByEmail(),
                borrowing.getReturnVerifiedAt(),
                borrowing.getConditionBefore(),
                borrowing.getConditionAfter(),
                borrowing.getHandoverProofUrl(),
                borrowing.getReturnProofUrl(),
                borrowing.getNote(),
                borrowing.isActive(),
                borrowing.getCreatedAt(),
                borrowing.getUpdatedAt()
        );
    }
}
