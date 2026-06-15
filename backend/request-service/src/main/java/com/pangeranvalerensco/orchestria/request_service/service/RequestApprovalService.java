package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.payload.request.ProcessApprovalRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;

public interface RequestApprovalService {

    FundRequestResponse approve(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail
    );

    FundRequestResponse reject(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail
    );

    FundRequestResponse requestRevision(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail
    );
}