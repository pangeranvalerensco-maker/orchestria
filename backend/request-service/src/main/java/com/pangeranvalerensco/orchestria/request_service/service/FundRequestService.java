package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateFundRequestRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.PageResponse;

public interface FundRequestService {
    
    FundRequestResponse create(CreateFundRequestRequest request, String currentUserEmail);

    PageResponse<FundRequestResponse> getAll(
            FundRequestStatus status,
            Long divisionId,
            Long requestMemberId,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );

    FundRequestResponse getById(Long id);

    FundRequestResponse submit(Long id, String currentUserEmail);
}
