package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EvidenceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DivisionTaskEvidenceResponse {

    private Long id;

    private Long taskId;
    private String taskTitle;

    private EvidenceType type;
    private String title;
    private String description;
    private String fileUrl;
    private String externalLink;
    private Long submittedByMemberId;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}