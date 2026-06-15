package com.pangeranvalerensco.orchestria.request_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "request_settlements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_request_id", nullable = false, unique = true)
    private FundRequest fundRequest;

    @Column(name = "spent_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal spentAmount;

    @Column(name = "remaining_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "shortage_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal shortageAmount;

    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "submitted_by_email", nullable = false, length = 150)
    private String submittedByEmail;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "approved_by_email", length = 150)
    private String approvedByEmail;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (remainingAmount == null) {
            remainingAmount = BigDecimal.ZERO;
        }

        if (shortageAmount == null) {
            shortageAmount = BigDecimal.ZERO;
        }

        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }

        if (active == null) {
            active = true;
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}