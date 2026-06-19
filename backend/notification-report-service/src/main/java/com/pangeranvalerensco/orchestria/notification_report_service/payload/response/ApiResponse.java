package com.pangeranvalerensco.orchestria.notification_report_service.payload.response;

import lombok.Builder;
import lombok.Data;

/**
 * Wrapper response standar untuk endpoint REST internal service ini.
 */
@Data
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
}
