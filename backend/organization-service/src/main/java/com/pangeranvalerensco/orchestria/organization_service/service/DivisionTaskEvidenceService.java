package com.pangeranvalerensco.orchestria.organization_service.service;

import com.pangeranvalerensco.orchestria.organization_service.payload.request.DivisionTaskEvidenceRequest;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.ApiResponse;
import com.pangeranvalerensco.orchestria.organization_service.payload.response.DivisionTaskEvidenceResponse;

import java.util.List;

public interface DivisionTaskEvidenceService {

    ApiResponse<List<DivisionTaskEvidenceResponse>> getEvidencesByTask(Long taskId);

    ApiResponse<DivisionTaskEvidenceResponse> getEvidenceById(Long id);

    ApiResponse<DivisionTaskEvidenceResponse> createEvidence(DivisionTaskEvidenceRequest request);

    ApiResponse<DivisionTaskEvidenceResponse> updateEvidence(Long id, DivisionTaskEvidenceRequest request);

    ApiResponse<Void> deleteEvidence(Long id);

    ApiResponse<DivisionTaskEvidenceResponse> createMyEvidence(DivisionTaskEvidenceRequest request);

    ApiResponse<DivisionTaskEvidenceResponse> updateMyEvidence(Long id, DivisionTaskEvidenceRequest request);

    ApiResponse<Void> deleteMyEvidence(Long id);
}