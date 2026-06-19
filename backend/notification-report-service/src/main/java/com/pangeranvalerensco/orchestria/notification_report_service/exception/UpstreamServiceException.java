package com.pangeranvalerensco.orchestria.notification_report_service.exception;

public class UpstreamServiceException extends RuntimeException {
    public UpstreamServiceException(String message) {
        super(message);
    }

    public UpstreamServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
