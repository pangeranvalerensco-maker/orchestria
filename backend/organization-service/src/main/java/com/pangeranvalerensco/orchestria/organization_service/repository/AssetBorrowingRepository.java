package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.AssetBorrowing;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetBorrowingRepository extends JpaRepository<AssetBorrowing, String> {

    @Query("SELECT b FROM AssetBorrowing b WHERE b.borrowerMemberId = :memberId AND b.active = true " +
            "AND (:status IS NULL OR b.status = :status) " +
            "ORDER BY b.createdAt DESC")
    Page<AssetBorrowing> findByBorrowerMemberIdAndActiveTrueOrderByCreatedAtDesc(
            @Param("memberId") String memberId,
            @Param("status") BorrowingStatus status,
            Pageable pageable);

    @Query("SELECT b FROM AssetBorrowing b WHERE b.active = true " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:assetId IS NULL OR b.asset.id = :assetId) " +
            "AND (:borrowerName IS NULL OR LOWER(b.borrowerName) LIKE LOWER(CONCAT('%', :borrowerName, '%'))) " +
            "ORDER BY b.createdAt DESC")
    Page<AssetBorrowing> findAllWithFilters(
            @Param("status") BorrowingStatus status,
            @Param("assetId") String assetId,
            @Param("borrowerName") String borrowerName,
            Pageable pageable);

    List<AssetBorrowing> findByAssetIdAndStatusInAndActiveTrue(String assetId, List<BorrowingStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM AssetBorrowing b WHERE b.id = :id AND b.active = true")
    Optional<AssetBorrowing> findByIdAndActiveTrueWithLock(@Param("id") String id);

    Optional<AssetBorrowing> findByIdAndActiveTrue(String id);
}
