package com.pangeranvalerensco.orchestria.request_service.payload.response;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestSettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RequestSettlementResponse {

    private Long id;

    private Long fundRequestId;

    private RequestSettlementStatus status;
    private BigDecimal requestedAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal shortageAmount;

    private String proofUrl;
    private String note;

    private Integer submissionCount;
    private Integer revisionCount;
    private String lastRevisionNote;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;

    private String submittedByEmail;
    private LocalDateTime submittedAt;

    private String approvedByEmail;
    private LocalDateTime approvedAt;

    private Long lockVersion;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
