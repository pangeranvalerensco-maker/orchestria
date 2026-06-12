package com.pangeranvalerensco.orchestria.request_service.payload.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestPriority;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FundRequestResponse {

    private Long id;

    private Long divisionId;
    private String divisionName;

    private Long requesterMemberId;
    private String requesterName;
    private Long requesterAuthUserId;

    private String title;
    private String description;
    private LocalDate activityDate;

    private RequestPriority priority;
    private FundRequestStatus status;

    private BigDecimal totalAmount;

    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    private Boolean active;

    private String createdByEmail;
    private String updatedByEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}