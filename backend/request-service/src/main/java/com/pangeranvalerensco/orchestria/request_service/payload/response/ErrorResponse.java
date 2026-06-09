package com.pangeranvalerensco.orchestria.request_service.payload.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse<T> {
    
    private Boolean success;
    private String message;
    private T errors;
    private String path;
    private LocalDateTime timestamp;
    
}
