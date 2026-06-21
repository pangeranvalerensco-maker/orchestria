package com.pangeranvalerensco.orchestria.organization_service.entity;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_borrowings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetBorrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String borrowerMemberId;

    private String borrowerAuthUserId;

    @Column(nullable = false)
    private String borrowerName;

    @Column(nullable = false)
    private String borrowerEmail;

    @Column(nullable = false, length = 1000)
    private String purpose;

    @Column(nullable = false)
    private LocalDate borrowDate;

    @Column(nullable = false)
    private LocalDate expectedReturnDate;

    private LocalDate actualReturnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;

    private String rejectionReason;

    private String cancellationReason;

    private String approvedByEmail;

    private LocalDateTime approvedAt;

    private String handedOverByEmail;

    private LocalDateTime handedOverAt;

    private LocalDateTime returnRequestedAt;

    private String returnVerifiedByEmail;

    private LocalDateTime returnVerifiedAt;

    @Enumerated(EnumType.STRING)
    private AssetCondition conditionBefore;

    @Enumerated(EnumType.STRING)
    private AssetCondition conditionAfter;

    private String handoverProofUrl;

    private String returnProofUrl;

    private String note;

    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
