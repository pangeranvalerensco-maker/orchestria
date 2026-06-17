package com.pangeranvalerensco.orchestria.finance_service.service;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import com.pangeranvalerensco.orchestria.finance_service.payload.request.CreateFundDisbursementRequest;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.FundDisbursementResponse;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.PageResponse;

public interface FundDisbursementService {

        FundDisbursementResponse create(
                        CreateFundDisbursementRequest request,
                        String currentUserEmail,
                        String authorizationHeader);

        FundDisbursementResponse getById(Long id);

        FundDisbursementResponse getByFundRequestId(Long fundRequestId);

        PageResponse<FundDisbursementResponse> getAll(
                        DisbursementStatus status,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection);

        FundDisbursementResponse retryRequestSync(
                        Long id,
                        String authorizationHeader);
}