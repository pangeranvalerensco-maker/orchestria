package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ArchiveDocumentResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

public interface ArchiveDocumentService {

    List<ArchiveDocumentResponse> listDocuments(String keyword, DocumentCategory category);

    ArchiveDocumentResponse getDocument(Long id);

    ArchiveDocumentResponse uploadDocument(
            String title,
            String description,
            DocumentCategory category,
            MultipartFile file,
            String uploaderEmail,
            String uploaderName
    );

    record DownloadResult(
            InputStream inputStream,
            String originalFileName,
            String contentType,
            long sizeBytes
    ) {}

    DownloadResult downloadDocument(Long id);

    void softDeleteDocument(Long id, String deleterEmail);
}
