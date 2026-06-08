package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.OrganizationPeriodRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.OrganizationPeriodResponse;

public interface OrganizationPeriodService {
    
    ApiResponse<List<OrganizationPeriodResponse>> getAllPeriods();

    ApiResponse<OrganizationPeriodResponse> getCurrentPeriod();

    ApiResponse<OrganizationPeriodResponse> getPeriodById(Long id);

    ApiResponse<OrganizationPeriodResponse> createPeriod(OrganizationPeriodRequest request);

    ApiResponse<OrganizationPeriodResponse> updatePeriod(Long id, OrganizationPeriodRequest request);

    ApiResponse<Void> deletePeriod(Long id);
}
