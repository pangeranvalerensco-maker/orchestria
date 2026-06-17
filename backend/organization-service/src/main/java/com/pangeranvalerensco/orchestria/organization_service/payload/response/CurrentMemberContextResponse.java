package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurrentMemberContextResponse {

    private MemberResponse member;

    private List<MemberAssignmentResponse> activeAssignments;
}