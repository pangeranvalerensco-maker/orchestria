package com.pangeranvalerensco.orchestria.request_service.service.impl;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestApproval;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestApprovalResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestStatusHistoryResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestApprovalRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.service.RequestTimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestTimelineServiceImpl implements RequestTimelineService {

    private final FundRequestRepository fundRequestRepository;
    private final RequestApprovalRepository requestApprovalRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RequestApprovalResponse> getApprovals(Long fundRequestId) {
        FundRequest fundRequest = findActiveFundRequest(fundRequestId);

        List<RequestApproval> approvals = requestApprovalRepository
                .findByFundRequestOrderByDecidedAtAsc(fundRequest);

        return approvals.stream()
                .map(approval -> RequestApprovalResponse.builder()
                        .id(approval.getId())
                        .level(approval.getLevel())
                        .decision(approval.getDecision())
                        .approverEmail(approval.getApproverEmail())
                        .approverName(approval.getApproverName())
                        .note(approval.getNote())
                        .decidedAt(approval.getDecidedAt())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestStatusHistoryResponse> getHistories(Long fundRequestId) {
        FundRequest fundRequest = findActiveFundRequest(fundRequestId);

        List<RequestStatusHistory> histories = requestStatusHistoryRepository
                .findByFundRequestOrderByChangedAtAsc(fundRequest);

        return histories.stream()
                .map(history -> RequestStatusHistoryResponse.builder()
                        .id(history.getId())
                        .oldStatus(history.getOldStatus())
                        .newStatus(history.getNewStatus())
                        .changedByEmail(history.getChangedByEmail())
                        .note(history.getNote())
                        .changedAt(history.getChangedAt())
                        .build())
                .toList();
    }

    private FundRequest findActiveFundRequest(Long fundRequestId) {
        return fundRequestRepository.findById(fundRequestId)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));
    }
}