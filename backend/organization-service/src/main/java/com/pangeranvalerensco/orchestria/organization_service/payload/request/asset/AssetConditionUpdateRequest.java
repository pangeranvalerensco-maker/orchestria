package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetConditionUpdateRequest(
        @NotNull AssetCondition newCondition,
        @NotNull AssetStatus newStatus,
        @Size(max = 1000) String note
) {}
