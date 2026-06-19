package com.pangeranvalerensco.orchestria.notification_report_service.exception;

import com.pangeranvalerensco.orchestria.notification_report_service.payload.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleUpstream(UpstreamServiceException exception) {
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .message(exception.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
    }
}
