package com.pangeranvalerensco.orchestria.request_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestSettlementRevisionRequest {

    @NotBlank(message = "Catatan revisi wajib diisi")
    @Size(max = 2000, message = "Catatan revisi maksimal 2000 karakter")
    private String revisionNote;
}
