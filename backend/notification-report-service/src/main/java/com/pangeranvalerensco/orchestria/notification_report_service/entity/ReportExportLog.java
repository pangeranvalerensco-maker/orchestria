package com.pangeranvalerensco.orchestria.notification_report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "report_export_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportExportLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "filename")
    private String filename;

    @Column(name = "requested_by_email", nullable = false)
    private String requestedByEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ReportExportStatus status;

    @Column(name = "record_count")
    private Integer recordCount;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_by_email")
    private String createdByEmail;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime finishedAt;

    @PrePersist
    public void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
