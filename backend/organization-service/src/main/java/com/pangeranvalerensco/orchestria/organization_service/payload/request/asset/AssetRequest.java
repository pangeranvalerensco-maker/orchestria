package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetRequest(
        @NotBlank @Size(max = 50) String assetCode,
        @NotBlank @Size(max = 150) String assetName,
        @NotBlank @Size(max = 100) String category,
        @Size(max = 1000) String description,
        @NotNull AssetCondition currentCondition,
        @Size(max = 200) String location,
        Long responsibleMemberId,
        @Size(max = 500) String imageUrl
) {}
