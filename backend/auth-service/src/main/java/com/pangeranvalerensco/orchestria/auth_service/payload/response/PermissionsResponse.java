package com.pangeranvalerensco.orchestria.auth_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionsResponse {
    
    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
