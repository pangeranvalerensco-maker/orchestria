package com.pangeranvalerensco.orchestria.request_service.entity;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_status_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relasi internal request-service.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_request_id", nullable = false)
    private FundRequest fundRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    private FundRequestStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    private FundRequestStatus newStatus;

    @Column(name = "changed_by_email", nullable = false, length = 150)
    private String changedByEmail;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}