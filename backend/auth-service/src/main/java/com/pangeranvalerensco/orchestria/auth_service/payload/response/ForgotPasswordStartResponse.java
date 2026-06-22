package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordStartResponse {
    private String challengeId;
    private int expiresInSeconds;
    private int resendAfterSeconds;
}
