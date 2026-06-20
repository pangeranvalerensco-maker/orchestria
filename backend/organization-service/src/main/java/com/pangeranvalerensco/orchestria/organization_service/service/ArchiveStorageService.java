package com.pangeranvalerensco.orchestria.organization_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface ArchiveStorageService {

    /**
     * Validates and stores the given file.
     *
     * @param file the uploaded file
     * @return StorageResult containing the UUID-based storedFileName and storageReference
     */
    StorageResult store(MultipartFile file);

    /**
     * Retrieves the file bytes as an InputStream.
     *
     * @param storageReference relative storage reference (not a client-provided path)
     * @return InputStream of the file content
     */
    InputStream retrieve(String storageReference);

    record StorageResult(String storedFileName, String storageReference) {}
}
