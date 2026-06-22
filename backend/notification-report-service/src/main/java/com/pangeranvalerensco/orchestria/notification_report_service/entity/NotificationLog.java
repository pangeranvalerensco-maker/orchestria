package com.pangeranvalerensco.orchestria.notification_report_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Convert(converter = StringListConverter.class)
    @Column(name = "to_recipients", columnDefinition = "TEXT")
    private List<String> toRecipients = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "cc_recipients", columnDefinition = "TEXT")
    private List<String> ccRecipients = new ArrayList<>();

    @Convert(converter = StringListConverter.class)
    @Column(name = "bcc_recipients", columnDefinition = "TEXT")
    private List<String> bccRecipients = new ArrayList<>();

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "html", nullable = false)
    private boolean html;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "created_by_email", nullable = false)
    private String createdByEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_attempt_at")
    private LocalDateTime lastAttemptAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

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
