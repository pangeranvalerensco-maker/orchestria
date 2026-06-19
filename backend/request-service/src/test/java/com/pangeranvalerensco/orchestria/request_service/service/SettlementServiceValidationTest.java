package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestSettlementStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.RequestSettlementRevisionRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestSettlementRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceValidationTest {

    private static final Long REQUEST_ID = 4L;

    @Mock
    private FundRequestRepository fundRequestRepository;

    @Mock
    private RequestSettlementRepository requestSettlementRepository;

    @Mock
    private RequestStatusHistoryRepository requestStatusHistoryRepository;

    @InjectMocks
    private SettlementService settlementService;

    private AuthenticatedUser owner;
    private AuthenticatedUser reviewer;

    @BeforeEach
    void setUp() {
        owner = new AuthenticatedUser(
                100L,
                "izhar.harahap@orchestria.local",
                "Izhar Harahap",
                List.of("ANGGOTA"));
        reviewer = new AuthenticatedUser(
                200L,
                "andini.siti.nuriyanti@orchestria.local",
                "Andini Siti Nuriyanti",
                List.of("BENDAHARA_INTERNAL"));
    }

    @Test
    void approvalWithoutUsageProofIsRejected() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(fundRequest);
        settlement.setProofUrl("   ");

        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));

        assertThrows(
                BadRequestException.class,
                () -> settlementService.approve(REQUEST_ID, reviewer));
    }

    @Test
    void spentAmountMustBeGreaterThanZero() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.FUND_RECEIVED);
        SubmitSettlementRequest request = new SubmitSettlementRequest();
        request.setSpentAmount(BigDecimal.ZERO);
        request.setProofUrl("https://example.com/struk.jpg");

        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(false);

        assertThrows(
                BadRequestException.class,
                () -> settlementService.submitFirst(REQUEST_ID, request, owner));
    }

    @Test
    void doubleRevisionRequestCannotSucceedTwice() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(fundRequest);

        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));
        when(requestSettlementRepository.save(any(RequestSettlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fundRequestRepository.save(any(FundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(requestStatusHistoryRepository.save(any(RequestStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RequestSettlementRevisionRequest request = new RequestSettlementRevisionRequest();
        request.setRevisionNote("Bukti transaksi perlu diperbaiki");

        settlementService.requestRevision(REQUEST_ID, request, reviewer);

        assertThrows(
                BadRequestException.class,
                () -> settlementService.requestRevision(REQUEST_ID, request, reviewer));

        verify(requestSettlementRepository, times(1)).save(settlement);
    }

    private FundRequest fundRequest(FundRequestStatus status) {
        return FundRequest.builder()
                .id(REQUEST_ID)
                .requesterAuthUserId(owner.userId())
                .requesterName(owner.fullName())
                .createdByEmail(owner.email())
                .updatedByEmail(owner.email())
                .totalAmount(new BigDecimal("1000000.00"))
                .status(status)
                .active(true)
                .build();
    }

    private RequestSettlement settlement(FundRequest fundRequest) {
        return RequestSettlement.builder()
                .id(10L)
                .fundRequest(fundRequest)
                .status(RequestSettlementStatus.SUBMITTED)
                .spentAmount(new BigDecimal("900000.00"))
                .remainingAmount(new BigDecimal("100000.00"))
                .shortageAmount(BigDecimal.ZERO)
                .proofUrl("https://example.com/struk.jpg")
                .submissionCount(1)
                .revisionCount(0)
                .submittedByEmail(owner.email())
                .active(true)
                .build();
    }
}
