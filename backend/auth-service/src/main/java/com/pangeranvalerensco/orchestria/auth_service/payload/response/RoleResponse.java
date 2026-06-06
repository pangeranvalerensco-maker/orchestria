package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleResponse {
    
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private Set<String> permissions;
}
