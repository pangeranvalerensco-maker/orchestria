package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SessionDemoResponse {
    private boolean authenticated;
    
    @Builder.Default
    private String authenticationMode = "STATEFUL_HTTP_SESSION";
    
    private SessionDemoUserResponse user;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;
    private Long expiresInSeconds;
    private String message;
}
