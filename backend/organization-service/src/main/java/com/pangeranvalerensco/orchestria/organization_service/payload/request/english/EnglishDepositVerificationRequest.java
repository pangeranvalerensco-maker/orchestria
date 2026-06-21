package com.pangeranvalerensco.orchestria.organization_service.payload.request.english;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record EnglishDepositVerificationRequest(
        @NotBlank(message = "Decision wajib diisi")
        @Pattern(regexp = "^(VERIFIED|REJECTED)$", message = "Decision hanya VERIFIED atau REJECTED")
        String decision,

        BigDecimal score,

        String verificationNote
) {
}
