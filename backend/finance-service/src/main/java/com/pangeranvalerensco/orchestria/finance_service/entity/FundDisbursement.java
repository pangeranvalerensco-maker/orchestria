package com.pangeranvalerensco.orchestria.finance_service.entity;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementMethod;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_disbursements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundDisbursement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference ke request-service.
     * Tidak pakai FK karena beda database.
     */
    @Column(name = "fund_request_id", nullable = false)
    private Long fundRequestId;

    @Column(name = "request_title", nullable = false, length = 150)
    private String requestTitle;

    @Column(name = "division_id", nullable = false)
    private Long divisionId;

    @Column(name = "division_name", nullable = false, length = 150)
    private String divisionName;

    @Column(name = "requester_name", nullable = false, length = 150)
    private String requesterName;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DisbursementMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DisbursementStatus status;

    @Column(name = "receiver_name", nullable = false, length = 150)
    private String receiverName;

    @Column(name = "receiver_note", columnDefinition = "TEXT")
    private String receiverNote;

    @Column(name = "proof_url", length = 500)
    private String proofUrl;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "disbursed_by_email", nullable = false, length = 150)
    private String disbursedByEmail;

    @Column(name = "disbursed_at", nullable = false)
    private LocalDateTime disbursedAt;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = DisbursementStatus.DISBURSED;
        }

        if (active == null) {
            active = true;
        }

        if (disbursedAt == null) {
            disbursedAt = LocalDateTime.now();
        }

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}