package com.pangeranvalerensco.orchestria.request_service.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubmitSettlementRequest {

    @NotNull(message = "spentAmount wajib diisi")
    @DecimalMin(value = "0.0", inclusive = false, message = "spentAmount harus lebih dari 0")
    private BigDecimal spentAmount;

    @NotBlank(message = "Bukti atau struk pembayaran wajib diisi")
    @Size(max = 500, message = "proofUrl maksimal 500 karakter")
    private String proofUrl;

    @Size(max = 2000, message = "Catatan maksimal 2000 karakter")
    private String note;
}
