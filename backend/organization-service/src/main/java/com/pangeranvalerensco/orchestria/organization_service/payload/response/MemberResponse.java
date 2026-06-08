package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.MemberStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MemberResponse {

    private Long id;
    private Long authUserId;
    private String fullName;
    private String email;
    private String studentNumber;
    private String phoneNumber;
    private String cohort;
    private String profilePhotoUrl;
    private String major;
    private String campusClass;
    private Boolean publicVisible;
    private Integer displayOrder;
    private MemberStatus status;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}