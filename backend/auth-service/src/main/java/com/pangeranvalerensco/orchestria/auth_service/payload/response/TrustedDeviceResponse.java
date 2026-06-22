package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TrustedDeviceResponse {
    private String id;
    private String deviceName;
    private String userAgent;
    private String lastIpAddress;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private LocalDateTime expiresAt;
}
