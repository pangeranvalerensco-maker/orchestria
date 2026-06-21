package com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.PointRecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PointRequest(
        @NotNull Long memberId,
        String scheduleId,
        @NotNull PointRecordType type,
        @NotNull @Positive Integer pointValue,
        @NotBlank String reason
) {}
