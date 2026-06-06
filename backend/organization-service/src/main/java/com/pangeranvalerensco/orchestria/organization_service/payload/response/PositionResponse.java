package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionResponse {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer levelOrder;
    private Boolean publicVisible;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
