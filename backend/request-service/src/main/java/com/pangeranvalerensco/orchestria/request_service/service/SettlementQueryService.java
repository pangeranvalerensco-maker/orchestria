package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestSettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettlementQueryService {

    private final FundRequestRepository fundRequestRepository;
    private final RequestSettlementRepository requestSettlementRepository;

    @Transactional(readOnly = true)
    public RequestSettlementResponse getByFundRequestId(Long fundRequestId) {
        FundRequest fundRequest = fundRequestRepository.findById(fundRequestId)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pengajuan dana tidak ditemukan"));

        RequestSettlement settlement = requestSettlementRepository
                .findByFundRequestAndActiveTrue(fundRequest)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Laporan penggunaan dana belum tersedia"));

        return RequestSettlementResponse.builder()
                .id(settlement.getId())
                .fundRequestId(fundRequest.getId())
                .requestedAmount(fundRequest.getTotalAmount())
                .spentAmount(settlement.getSpentAmount())
                .remainingAmount(settlement.getRemainingAmount())
                .shortageAmount(settlement.getShortageAmount())
                .proofUrl(settlement.getProofUrl())
                .note(settlement.getNote())
                .submittedByEmail(settlement.getSubmittedByEmail())
                .submittedAt(settlement.getSubmittedAt())
                .approvedByEmail(settlement.getApprovedByEmail())
                .approvedAt(settlement.getApprovedAt())
                .active(settlement.getActive())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }
}
