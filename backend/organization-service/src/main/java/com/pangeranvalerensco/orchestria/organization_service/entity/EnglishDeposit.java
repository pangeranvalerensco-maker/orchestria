package com.pangeranvalerensco.orchestria.organization_service.entity;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishDepositStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "english_deposits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnglishDeposit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private EnglishActivity activity;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private String memberName;

    @Column(nullable = false)
    private String memberEmail;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, length = 1000)
    private String evidenceUrl;

    @Column(columnDefinition = "TEXT")
    private String submissionNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnglishDepositStatus status;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(columnDefinition = "TEXT")
    private String verificationNote;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private String verifiedByEmail;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
