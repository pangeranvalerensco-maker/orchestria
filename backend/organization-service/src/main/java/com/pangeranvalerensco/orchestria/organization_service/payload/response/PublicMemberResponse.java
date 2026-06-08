package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicMemberResponse {

    private Long id;
    private String fullName;
    private String studentNumber;
    private String cohort;
    private String profilePhotoUrl;
    private String major;
    private String campusClass;
}
