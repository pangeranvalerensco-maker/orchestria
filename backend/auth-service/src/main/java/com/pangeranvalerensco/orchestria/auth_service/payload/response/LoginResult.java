package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResult {
    private LoginStatus status;
    private AuthResponse authData;
    
    // For OTP_REQUIRED
    private String challengeId;
    private String maskedEmail;
    private Integer expiresInSeconds;
    private Integer resendAfterSeconds;
}
