package com.pangeranvalerensco.orchestria.organization_service.service.impl;

import com.pangeranvalerensco.orchestria.organization_service.entity.ArchiveDocument;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import com.pangeranvalerensco.orchestria.organization_service.exception.ArchiveDocumentNotFoundException;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ArchiveDocumentResponse;
import com.pangeranvalerensco.orchestria.organization_service.repository.ArchiveDocumentRepository;
import com.pangeranvalerensco.orchestria.organization_service.service.ArchiveDocumentService;
import com.pangeranvalerensco.orchestria.organization_service.service.ArchiveStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArchiveDocumentServiceImpl implements ArchiveDocumentService {

    private final ArchiveDocumentRepository repository;
    private final ArchiveStorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public List<ArchiveDocumentResponse> listDocuments(String keyword, DocumentCategory category) {
        String kw = (keyword != null && keyword.isBlank()) ? null : keyword;
        return repository.findActiveDocuments(kw, category)
                .stream()
                .map(ArchiveDocumentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ArchiveDocumentResponse getDocument(Long id) {
        ArchiveDocument doc = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ArchiveDocumentNotFoundException(id));
        return ArchiveDocumentResponse.from(doc);
    }

    @Override
    @Transactional
    public ArchiveDocumentResponse uploadDocument(
            String title,
            String description,
            DocumentCategory category,
            MultipartFile file,
            String uploaderEmail,
            String uploaderName
    ) {

    }

    @Override
    @Transactional(readOnly = true)
    public DownloadResult downloadDocument(Long id) {
        ArchiveDocument doc = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ArchiveDocumentNotFoundException(id));

        InputStream inputStream = storageService.retrieve(doc.getStorageReference());

        return new DownloadResult(
                inputStream,
                doc.getOriginalFileName(),
                doc.getContentType(),
                doc.getSizeBytes()
        );
    }

    @Override
    @Transactional
    public void softDeleteDocument(Long id, String deleterEmail) {
        ArchiveDocument doc = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ArchiveDocumentNotFoundException(id));

        doc.setDeleted(true);
        doc.setDeletedAt(LocalDateTime.now());
        doc.setDeletedByEmail(deleterEmail);

        repository.save(doc);
        log.info("Archive document soft-deleted: id={}, by={}", id, deleterEmail);
    }
}
