package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {
    
    private Long id;
    private String fullName;
    private String email;
    private Boolean active;
    private Set<String> roles;
    private Set<String> permissions;
    private Boolean twoFactorEnabled;
    private Boolean twoFactorRequired;
}
