package com.pangeranvalerensco.orchestria.request_service.entity;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestPriority;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fund_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference ke organization-service.
     * Tidak pakai FK lintas database.
     */
    @Column(name = "division_id", nullable = false)
    private Long divisionId;

    @Column(name = "division_name", nullable = false, length = 150)
    private String divisionName;

    /**
     * Reference ke organization-service member.
     */
    @Column(name = "requester_member_id", nullable = false)
    private Long requesterMemberId;

    @Column(name = "requester_name", nullable = false, length = 150)
    private String requesterName;

    /**
     * Reference ke auth-service user.
     */
    @Column(name = "requester_auth_user_id")
    private Long requesterAuthUserId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "activity_date")
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private RequestPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FundRequestStatus status;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "created_by_email", nullable = false, length = 150)
    private String createdByEmail;

    @Column(name = "updated_by_email", length = 150)
    private String updatedByEmail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (priority == null) {
            priority = RequestPriority.MEDIUM;
        }

        if (status == null) {
            status = FundRequestStatus.DRAFT;
        }

        if (totalAmount == null) {
            totalAmount = BigDecimal.ZERO;
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