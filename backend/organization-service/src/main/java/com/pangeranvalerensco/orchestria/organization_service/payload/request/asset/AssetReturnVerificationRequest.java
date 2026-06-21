package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetReturnVerificationRequest(
        @NotNull AssetCondition conditionAfter,
        @Size(max = 1000) String note
) {}
