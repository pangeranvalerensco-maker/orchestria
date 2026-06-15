package com.pangeranvalerensco.orchestria.finance_service.payload.response;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementMethod;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class FundDisbursementResponse {

    private Long id;

    private Long fundRequestId;
    private String requestTitle;

    private Long divisionId;
    private String divisionName;

    private String requesterName;

    private BigDecimal amount;

    private DisbursementMethod method;
    private DisbursementStatus status;

    private String receiverName;
    private String receiverNote;

    private String proofUrl;
    private String note;

    private String disbursedByEmail;
    private LocalDateTime disbursedAt;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}