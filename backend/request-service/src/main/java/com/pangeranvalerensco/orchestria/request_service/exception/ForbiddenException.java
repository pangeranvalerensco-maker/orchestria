package com.pangeranvalerensco.orchestria.request_service.exception;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}