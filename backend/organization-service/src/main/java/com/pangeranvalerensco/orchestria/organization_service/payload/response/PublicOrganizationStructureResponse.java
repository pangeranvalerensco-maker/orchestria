package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicOrganizationStructureResponse {
    
    private OrganizationPeriodResponse period;
    private List<PublicMemberAssignmentResponse> structure;
}
