package com.pangeranvalerensco.orchestria.organization_service.exception;

public class ArchiveDocumentNotFoundException extends RuntimeException {

    public ArchiveDocumentNotFoundException(Long id) {
        super("Dokumen arsip dengan id " + id + " tidak ditemukan atau sudah dihapus");
    }
}
