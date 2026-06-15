package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateRequestItemRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;

public interface RequestItemService {

    FundRequestResponse addItem(
            Long fundRequestId,
            CreateRequestItemRequest request,
            String currentUserEmail
    );
}