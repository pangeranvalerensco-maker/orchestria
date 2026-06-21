package com.pangeranvalerensco.orchestria.organization_service.payload.response.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;

import java.time.LocalDateTime;

public record AssetResponse(
        String id,
        String assetCode,
        String assetName,
        String category,
        String description,
        AssetStatus currentStatus,
        AssetCondition currentCondition,
        String location,
        String responsibleMemberId,
        String imageUrl,
        boolean active,
        boolean available,
        String activeBorrowingId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
