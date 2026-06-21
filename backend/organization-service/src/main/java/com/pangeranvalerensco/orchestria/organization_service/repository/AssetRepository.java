package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.Asset;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, String> {

    Optional<Asset> findByAssetCodeIgnoreCase(String assetCode);

    boolean existsByAssetCodeIgnoreCase(String assetCode);

    Page<Asset> findByActiveTrue(Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.active = true " +
            "AND a.currentStatus != com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus.LOST " +
            "AND (:search IS NULL OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.assetName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR a.currentStatus = :status) " +
            "AND (:condition IS NULL OR a.currentCondition = :condition)")
    Page<Asset> searchActiveAssets(@Param("search") String search,
                                   @Param("status") AssetStatus status,
                                   @Param("condition") AssetCondition condition,
                                   Pageable pageable);

    @Query("SELECT a FROM Asset a WHERE a.active = true " +
            "AND (:search IS NULL OR LOWER(a.assetCode) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.assetName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(a.category) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR a.currentStatus = :status) " +
            "AND (:condition IS NULL OR a.currentCondition = :condition)")
    Page<Asset> searchAllAssets(@Param("search") String search,
                                @Param("status") AssetStatus status,
                                @Param("condition") AssetCondition condition,
                                Pageable pageable);

    Optional<Asset> findByIdAndActiveTrue(String id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id AND a.active = true")
    Optional<Asset> findByIdAndActiveTrueWithLock(@Param("id") String id);
}
