package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import com.pangeranvalerensco.orchestria.organization_service.entity.ArchiveDocument;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDocumentResponse {

    private Long id;
    private String title;
    private String description;
    private DocumentCategory category;
    private String originalFileName;
    private String contentType;
    private Long sizeBytes;
    private String uploadedByEmail;
    private String uploadedByName;
    private LocalDateTime uploadedAt;
    private Boolean deleted;

    // storedFileName, storageReference, dan physical path TIDAK dikembalikan

    public static ArchiveDocumentResponse from(ArchiveDocument doc) {
        return ArchiveDocumentResponse.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .category(doc.getCategory())
                .originalFileName(doc.getOriginalFileName())
                .contentType(doc.getContentType())
                .sizeBytes(doc.getSizeBytes())
                .uploadedByEmail(doc.getUploadedByEmail())
                .uploadedByName(doc.getUploadedByName())
                .uploadedAt(doc.getUploadedAt())
                .deleted(doc.getDeleted())
                .build();
    }
}
