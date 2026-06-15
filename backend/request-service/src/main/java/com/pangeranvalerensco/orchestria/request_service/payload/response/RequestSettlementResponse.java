package com.pangeranvalerensco.orchestria.request_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RequestSettlementResponse {

    private Long id;

    private Long fundRequestId;

    private BigDecimal requestedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal shortageAmount;

    private String proofUrl;
    private String note;

    private String submittedByEmail;
    private LocalDateTime submittedAt;

    private String approvedByEmail;
    private LocalDateTime approvedAt;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}