package com.pangeranvalerensco.orchestria.request_service.payload.response;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalDecision;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RequestApprovalResponse {

    private Long id;

    private ApprovalLevel level;
    private ApprovalDecision decision;

    private String approverEmail;
    private String approverName;

    private String note;
    private LocalDateTime decidedAt;
}