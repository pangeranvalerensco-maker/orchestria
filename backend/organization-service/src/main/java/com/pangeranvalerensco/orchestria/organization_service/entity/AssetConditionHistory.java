package com.pangeranvalerensco.orchestria.organization_service.entity;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "asset_condition_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetConditionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "borrowing_id")
    private AssetBorrowing borrowing;

    @Enumerated(EnumType.STRING)
    private AssetStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus newStatus;

    @Enumerated(EnumType.STRING)
    private AssetCondition oldCondition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetCondition newCondition;

    @Column(nullable = false)
    private String checkedByEmail;

    private String note;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
