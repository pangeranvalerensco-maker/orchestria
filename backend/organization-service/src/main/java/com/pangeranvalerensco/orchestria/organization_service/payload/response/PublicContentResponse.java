package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentEntry;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import com.pangeranvalerensco.orchestria.organization_service.entity.PublicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class PublicContentResponse {
    private String id;
    private PublicContentType contentType;
    private String title;
    private String subtitle;
    private String body;
    private String category;
    private String statusLabel;
    private LocalDate eventDate;
    private String mediaUrl;
    private String linkUrl;
    private String authorName;
    private String authorRole;
    private Integer displayOrder;
    private PublicationStatus publicationStatus;
    private boolean active;
    private String createdByEmail;
    private String updatedByEmail;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PublicContentResponse fromEntity(PublicContentEntry entity) {
        return PublicContentResponse.builder()
                .id(entity.getId())
                .contentType(entity.getContentType())
                .title(entity.getTitle())
                .subtitle(entity.getSubtitle())
                .body(entity.getBody())
                .category(entity.getCategory())
                .statusLabel(entity.getStatusLabel())
                .eventDate(entity.getEventDate())
                .mediaUrl(entity.getMediaUrl())
                .linkUrl(entity.getLinkUrl())
                .authorName(entity.getAuthorName())
                .authorRole(entity.getAuthorRole())
                .displayOrder(entity.getDisplayOrder())
                .publicationStatus(entity.getPublicationStatus())
                .active(entity.isActive())
                .createdByEmail(entity.getCreatedByEmail())
                .updatedByEmail(entity.getUpdatedByEmail())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
