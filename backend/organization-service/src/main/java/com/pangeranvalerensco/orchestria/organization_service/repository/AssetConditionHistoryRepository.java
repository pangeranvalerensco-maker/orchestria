package com.pangeranvalerensco.orchestria.organization_service.repository;

import com.pangeranvalerensco.orchestria.organization_service.entity.AssetConditionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetConditionHistoryRepository extends JpaRepository<AssetConditionHistory, String> {

    List<AssetConditionHistory> findByAssetIdOrderByCreatedAtDesc(String assetId);
}
