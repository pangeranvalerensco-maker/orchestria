package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicMemberAssignmentResponse {
    
    private Long memberId;
    private String memberName;
    private String cohort;
    private String profilePhotoUrl;
    private String major;

    private Long periodId;
    private String periodName;

    private Long divisionId;
    private String divisionCode;
    private String divisionName;

    private Long positionId;
    private String positionCode;
    private String positionName;
    private Integer positionLevelOrder;
}
