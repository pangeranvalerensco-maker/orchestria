package com.pangeranvalerensco.orchestria.organization_service.exception;

public class ArchiveUnsupportedMediaTypeException extends RuntimeException {

    public ArchiveUnsupportedMediaTypeException(String type) {
        super("Tipe file tidak didukung: " + type
                + ". Tipe yang diizinkan: PDF, DOCX, XLSX, PNG, JPEG");
    }
}
