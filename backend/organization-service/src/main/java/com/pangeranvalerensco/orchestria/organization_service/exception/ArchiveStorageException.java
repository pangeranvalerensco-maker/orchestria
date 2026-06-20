package com.pangeranvalerensco.orchestria.organization_service.exception;

public class ArchiveStorageException extends RuntimeException {

    public ArchiveStorageException(String message) {
        super(message);
    }

    public ArchiveStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
