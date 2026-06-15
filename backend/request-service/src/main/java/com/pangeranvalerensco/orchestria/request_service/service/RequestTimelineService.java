package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestApprovalResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestStatusHistoryResponse;

import java.util.List;

public interface RequestTimelineService {

    List<RequestApprovalResponse> getApprovals(Long fundRequestId);

    List<RequestStatusHistoryResponse> getHistories(Long fundRequestId);
}