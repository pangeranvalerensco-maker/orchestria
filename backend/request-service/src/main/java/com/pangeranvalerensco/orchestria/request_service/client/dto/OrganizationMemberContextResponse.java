package com.pangeranvalerensco.orchestria.request_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganizationMemberContextResponse {

    private MemberData member;
    private List<AssignmentData> activeAssignments;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemberData {

        private Long id;
        private Long authUserId;
        private String fullName;
        private String email;
        private Boolean active;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssignmentData {

        private Long divisionId;
        private String divisionCode;
        private String divisionName;
        private String positionCode;
        private String positionName;
        private Boolean active;
    }
}