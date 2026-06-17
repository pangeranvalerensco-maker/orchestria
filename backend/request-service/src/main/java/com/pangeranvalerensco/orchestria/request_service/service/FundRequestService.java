package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateFundRequestRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;

public interface FundRequestService {

    FundRequestResponse create(CreateFundRequestRequest request, AuthenticatedUser currentUserEmail);

    PageResponse<FundRequestResponse> getAll(
            FundRequestStatus status,
            Long divisionId,
            Long requestMemberId,
            int page,
            int size,
            String sortBy,
            String sortDirection);

    FundRequestResponse getById(Long id);

    FundRequestResponse submit(Long id, String currentUserEmail);

    FundRequestResponse markDisbursed(Long id, String currentUserEmail);

    FundRequestResponse markFundReceived(Long id, String currentUserEmail);

    RequestSettlementResponse submitSettlement(
            Long id,
            SubmitSettlementRequest request,
            String currentUserEmail);

    RequestSettlementResponse approveSettlement(Long id, String currentUserEmail);

    PageResponse<FundRequestResponse> getMyRequests(
            String currentUserEmail,
            int page,
            int size,
            String sortBy,
            String sortDirection);
}
