package com.pangeranvalerensco.orchestria.request_service.entity;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalDecision;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "request_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestApproval {

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
    @Column(nullable = false, length = 50)
    private ApprovalLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApprovalDecision decision;

    @Column(name = "approver_email", nullable = false, length = 150)
    private String approverEmail;

    @Column(name = "approver_name", length = 150)
    private String approverName;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        if (decidedAt == null) {
            decidedAt = LocalDateTime.now();
        }
    }
}