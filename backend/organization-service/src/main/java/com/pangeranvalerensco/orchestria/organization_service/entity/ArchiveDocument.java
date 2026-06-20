package com.pangeranvalerensco.orchestria.organization_service.entity;

import jakarta.persistence.*;
import lombok.*;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.DocumentCategory;

import java.time.LocalDateTime;

@Entity
@Table(name = "archive_documents")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DocumentCategory category;

    @Column(nullable = false, length = 500)
    private String originalFileName;

    @Column(nullable = false, length = 500)
    private String storedFileName;

    @Column(nullable = false, length = 200)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Column(nullable = false, length = 1000)
    private String storageReference;

    @Column(nullable = false, length = 500)
    private String uploadedByEmail;

    @Column(length = 500)
    private String uploadedByName;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Builder.Default
    @Column(nullable = false)
    private Boolean deleted = false;

    private LocalDateTime deletedAt;

    @Column(length = 500)
    private String deletedByEmail;

    @Version
    private Long lockVersion;
}
