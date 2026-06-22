package com.pangeranvalerensco.orchestria.auth_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @NotBlank
    private String resetToken;
    
    @NotBlank
    private String newPassword;
    
    @NotBlank
    private String confirmPassword;
}
