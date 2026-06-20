package com.pangeranvalerensco.orchestria.organization_service.exception;

public class ArchiveFileTooLargeException extends RuntimeException {

    private final long maxBytes;

    public ArchiveFileTooLargeException(long maxBytes) {
        super("Ukuran file melebihi batas maksimum "
                + (maxBytes / (1024 * 1024)) + " MB");
        this.maxBytes = maxBytes;
    }

    public long getMaxBytes() {
        return maxBytes;
    }
}
