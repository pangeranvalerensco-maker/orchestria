package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse<T> {

    private Boolean success;
    private Integer status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private T details;
    
}
