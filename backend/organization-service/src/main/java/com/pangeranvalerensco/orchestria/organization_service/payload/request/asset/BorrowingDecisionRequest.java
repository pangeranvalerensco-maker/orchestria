package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BorrowingDecisionRequest(
        @NotBlank @Size(max = 1000) String reason
) {}
