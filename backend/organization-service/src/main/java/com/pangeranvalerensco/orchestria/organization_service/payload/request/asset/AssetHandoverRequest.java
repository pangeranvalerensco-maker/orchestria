package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetHandoverRequest(
        @NotNull AssetCondition conditionBefore,
        @Size(max = 500) String handoverProofUrl,
        @Size(max = 1000) String note
) {}
