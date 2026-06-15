package com.pangeranvalerensco.orchestria.request_service.payload.response;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestStatusHistoryResponse {

    private Long id;

    private FundRequestStatus oldStatus;
    private FundRequestStatus newStatus;

    private String changedByEmail;
    private String note;

    private LocalDateTime changedAt;
}