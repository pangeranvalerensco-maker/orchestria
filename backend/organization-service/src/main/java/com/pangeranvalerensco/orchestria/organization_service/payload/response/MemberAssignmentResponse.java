package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.time.LocalDateTime;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberAssignmentResponse {

    private Long id;

    private Long memberId;
    private String memberName;
    private String memberEmail;
    private String cohort;

    private Long periodId;
    private String periodName;

    private Long divisionId;
    private String divisionCode;
    private String divisionName;

    private Long positionId;
    private String positionCode;
    private String positionName;
    private Integer positionLevelOrder;

    private AssignmentStatus status;
    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
