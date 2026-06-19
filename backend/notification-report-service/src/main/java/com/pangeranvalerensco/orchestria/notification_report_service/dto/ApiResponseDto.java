package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper generik ApiResponse dari request-service.
 * Digunakan untuk deserialisasi hasil REST call.
 */
@Data
@NoArgsConstructor
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
}
