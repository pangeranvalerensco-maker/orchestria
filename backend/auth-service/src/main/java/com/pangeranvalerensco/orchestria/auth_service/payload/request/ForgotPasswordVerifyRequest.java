package com.pangeranvalerensco.orchestria.auth_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordVerifyRequest {
    @NotBlank
    private String challengeId;
    
    @NotBlank
    private String code;
}
