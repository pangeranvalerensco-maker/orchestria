package com.pangeranvalerensco.orchestria.organization_service.controller;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ArchiveDocumentResponse;
import com.pangeranvalerensco.orchestria.organization_service.service.ArchiveDocumentService;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/organization/archive/documents")
@RequiredArgsConstructor
public class ArchiveDocumentController {

    private final ArchiveDocumentService archiveDocumentService;

    /**
     * GET /api/organization/archive/documents?keyword=&category=
     * Mengembalikan daftar dokumen aktif (belum deleted).
     */
    @GetMapping
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<ApiResponse<List<ArchiveDocumentResponse>>> listDocuments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) DocumentCategory category
    ) {
        List<ArchiveDocumentResponse> docs =
                archiveDocumentService.listDocuments(keyword, category);

        return ResponseEntity.ok(ApiResponse.<List<ArchiveDocumentResponse>>builder()
                .success(true)
                .message("Berhasil mengambil daftar dokumen arsip")
                .data(docs)
                .build());
    }

    /**
     * GET /api/organization/archive/documents/categories
     * Mengembalikan seluruh nilai enum DocumentCategory.
     */
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<ApiResponse<List<String>>> getCategories() {
        List<String> categories = Arrays.stream(DocumentCategory.values())
                .map(Enum::name)
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Berhasil mengambil daftar kategori")
                .data(categories)
                .build());
    }

    /**
     * GET /api/organization/archive/documents/{id}
     * Mengembalikan metadata dokumen aktif.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<ApiResponse<ArchiveDocumentResponse>> getDocument(
            @PathVariable Long id
    ) {
        ArchiveDocumentResponse doc = archiveDocumentService.getDocument(id);

        return ResponseEntity.ok(ApiResponse.<ArchiveDocumentResponse>builder()
                .success(true)
                .message("Berhasil mengambil dokumen arsip")
                .data(doc)
                .build());
    }

    /**
     * POST /api/organization/archive/documents (multipart/form-data)
     * Uploader diambil dari JWT/Authentication — bukan dari request body.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<ApiResponse<ArchiveDocumentResponse>> uploadDocument(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam DocumentCategory category,
            @RequestParam MultipartFile file,
            Authentication authentication
    ) {
        String uploaderEmail = authentication.getName();

        ArchiveDocumentResponse response = archiveDocumentService.uploadDocument(
                title,
                description,
                category,
                file,
                uploaderEmail,
                null
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ArchiveDocumentResponse>builder()
                        .success(true)
                        .message("Dokumen arsip berhasil diupload")
                        .data(response)
                        .build());
    }

    /**
     * GET /api/organization/archive/documents/{id}/download
     * Mengembalikan byte file dengan Content-Disposition attachment.
     */
    @GetMapping("/{id}/download")
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<InputStreamResource> downloadDocument(@PathVariable Long id) {
        ArchiveDocumentService.DownloadResult result =
                archiveDocumentService.downloadDocument(id);

        // Sanitize filename untuk header — jangan kebocorkan stored path
        String safeFilename = result.originalFileName()
                .replaceAll("[^a-zA-Z0-9._\\-]", "_");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.sizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + safeFilename + "\"")
                .body(new InputStreamResource(result.inputStream()));
    }

    /**
     * DELETE /api/organization/archive/documents/{id}
     * Soft delete — file fisik tidak langsung dihapus.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('archive.manage')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String deleterEmail = authentication.getName();
        archiveDocumentService.softDeleteDocument(id, deleterEmail);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Dokumen arsip berhasil dihapus")
                .data(null)
                .build());
    }
}
