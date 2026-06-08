package com.pangeranvalerensco.orchestria.organization_service.service;

import java.util.List;

import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.MemberAssignmentResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.request.MemberAssignmentRequest;

public interface MemberAssignmentService {
    
    ApiResponse<List<MemberAssignmentResponse>> getAllAssignment();

    ApiResponse<List<MemberAssignmentResponse>> getAssignmentByPeriod(Long periodId);

    ApiResponse<List<MemberAssignmentResponse>> getAssignmentByPeriodAndDivision(Long periodId, Long divisionId);

    ApiResponse<MemberAssignmentResponse> getAssignmentById(Long id);

    ApiResponse<MemberAssignmentResponse> createAssignment(MemberAssignmentRequest request);

    ApiResponse<MemberAssignmentResponse> updateAssignment(Long id, MemberAssignmentRequest request);

    ApiResponse<Void> deleteAssignment(Long id);
}
