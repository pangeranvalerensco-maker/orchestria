package com.pangeranvalerensco.orchestria.organization_service.payload.response.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;

import java.time.LocalDateTime;

public record ConditionHistoryResponse(
        String id,
        String assetId,
        String borrowingId,
        AssetStatus oldStatus,
        AssetStatus newStatus,
        AssetCondition oldCondition,
        AssetCondition newCondition,
        String checkedByEmail,
        String note,
        LocalDateTime checkedAt,
        LocalDateTime createdAt
) {}
