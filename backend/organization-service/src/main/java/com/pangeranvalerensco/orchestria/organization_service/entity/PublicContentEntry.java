package com.pangeranvalerensco.orchestria.organization_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "public_content_entries")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PublicContentEntry {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicContentType contentType;

    @Column(length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String statusLabel;

    private LocalDate eventDate;

    @Column(length = 2000)
    private String mediaUrl;

    @Column(length = 2000)
    private String linkUrl;

    @Column(length = 255)
    private String authorName;

    @Column(length = 255)
    private String authorRole;

    @Column(nullable = false)
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PublicationStatus publicationStatus;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 100)
    private String createdByEmail;

    @Column(length = 100)
    private String updatedByEmail;

    private LocalDateTime publishedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
