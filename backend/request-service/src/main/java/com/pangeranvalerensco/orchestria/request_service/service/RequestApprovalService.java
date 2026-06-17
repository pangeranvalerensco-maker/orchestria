package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.payload.request.ProcessApprovalRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;

public interface RequestApprovalService {

    FundRequestResponse approve(
            Long fundRequestId,
            ProcessApprovalRequest request,
            AuthenticatedUser currentUser
    );

    FundRequestResponse reject(
            Long fundRequestId,
            ProcessApprovalRequest request,
            AuthenticatedUser currentUser
    );

    FundRequestResponse requestRevision(
            Long fundRequestId,
            ProcessApprovalRequest request,
            AuthenticatedUser currentUser
    );
}