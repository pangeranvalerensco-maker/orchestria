package com.pangeranvalerensco.orchestria.organization_service.payload.request.asset;

import jakarta.validation.constraints.Size;

public record AssetReturnRequest(
        @Size(max = 500) String returnProofUrl,
        @Size(max = 1000) String note
) {}
