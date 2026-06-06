package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.PositionRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.PositionResponse;

public interface PositionService {
    
    ApiResponse<List<PositionResponse>> getAllPositions();
    ApiResponse<PositionResponse> getPositionById(Long id);
    ApiResponse<PositionResponse> createPosition(PositionRequest request);
    ApiResponse<PositionResponse> updatePosition(Long id, PositionRequest request);
    ApiResponse<Void> deletePosition(Long id);
}
