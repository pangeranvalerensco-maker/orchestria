package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtpResendResponse {
    private Integer expiresInSeconds;
    private Integer resendAfterSeconds;
}
