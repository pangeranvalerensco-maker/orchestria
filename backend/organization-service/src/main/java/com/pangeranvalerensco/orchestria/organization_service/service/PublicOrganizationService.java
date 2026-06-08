package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicMemberResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PublicOrganizationStructureResponse;

public interface PublicOrganizationService {

    ApiResponse<List<OrganizationPeriodResponse>> getPublicPeriods();

    ApiResponse<OrganizationPeriodResponse> getCurrentPeriod();

    ApiResponse<List<PublicMemberResponse>> getPublicMembersByCohort(String cohort);

    ApiResponse<PublicOrganizationStructureResponse> getCurrentStructure();

    ApiResponse<PublicOrganizationStructureResponse> getStructureByPeriod(Long periodId);
}
