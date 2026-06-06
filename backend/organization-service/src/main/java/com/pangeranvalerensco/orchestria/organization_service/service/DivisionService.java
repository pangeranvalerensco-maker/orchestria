package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionResponse;

public interface DivisionService {

    ApiResponse<List<DivisionResponse>> getAllDivisions();

    ApiResponse<DivisionResponse> getDivisionById(Long id);
    
    ApiResponse<DivisionResponse> createDivision(DivisionRequest request);

    ApiResponse<DivisionResponse> updateDivision(Long id, DivisionRequest request);

    ApiResponse<Void> deleteDivision(Long id);
}
