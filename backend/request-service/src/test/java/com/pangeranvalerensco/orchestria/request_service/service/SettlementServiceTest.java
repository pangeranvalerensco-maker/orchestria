package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestSettlementStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.RequestSettlementRevisionRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    private static final Long REQUEST_ID = 4L;
    private static final String OWNER_EMAIL = "izhar.harahap@orchestria.local";
    private static final String REVIEWER_EMAIL = "andini.siti.nuriyanti@orchestria.local";

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
                OWNER_EMAIL,
                "Izhar Harahap",
                List.of("ANGGOTA"));
        reviewer = new AuthenticatedUser(
                200L,
                REVIEWER_EMAIL,
                "Andini Siti Nuriyanti",
                List.of("BENDAHARA_INTERNAL"));
    }

    @Test
    void firstSubmitCreatesSubmittedSettlement() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.FUND_RECEIVED);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(false);
        stubSaves();

        RequestSettlementResponse response = settlementService.submitFirst(
                REQUEST_ID,
                validSettlementRequest(),
                owner);

        assertEquals(RequestSettlementStatus.SUBMITTED, response.getStatus());
        assertEquals(1, response.getSubmissionCount());
        assertEquals(0, response.getRevisionCount());
        assertEquals(FundRequestStatus.SETTLEMENT_SUBMITTED, fundRequest.getStatus());
        assertEquals(new BigDecimal("100000.00"), response.getRemainingAmount());
        assertEquals(BigDecimal.ZERO, response.getShortageAmount());
        verify(requestSettlementRepository).save(any(RequestSettlement.class));
        verify(requestStatusHistoryRepository).save(any(RequestStatusHistory.class));
    }

    @Test
    void firstSubmitWithoutProofIsRejected() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.FUND_RECEIVED);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(false);

        SubmitSettlementRequest request = validSettlementRequest();
        request.setProofUrl("   ");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> settlementService.submitFirst(REQUEST_ID, request, owner));

        assertEquals("Bukti penggunaan dana wajib diisi", exception.getMessage());
        verify(requestSettlementRepository, never()).save(any(RequestSettlement.class));
        verify(fundRequestRepository, never()).save(any(FundRequest.class));
    }

    @Test
    void nonHttpsProofIsRejected() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.FUND_RECEIVED);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(false);

        SubmitSettlementRequest request = validSettlementRequest();
        request.setProofUrl("http://example.com/struk.jpg");

        assertThrows(
                BadRequestException.class,
                () -> settlementService.submitFirst(REQUEST_ID, request, owner));
    }

    @Test
    void reviewerCanRequestRevision() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(
                fundRequest,
                RequestSettlementStatus.SUBMITTED,
                1,
                0);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));
        stubSaves();

        RequestSettlementRevisionRequest request = new RequestSettlementRevisionRequest();
        request.setRevisionNote("Struk belum memperlihatkan tanggal transaksi");

        RequestSettlementResponse response = settlementService.requestRevision(
                REQUEST_ID,
                request,
                reviewer);

        assertEquals(RequestSettlementStatus.REVISION_REQUIRED, response.getStatus());
        assertEquals(1, response.getRevisionCount());
        assertEquals(request.getRevisionNote(), response.getLastRevisionNote());
        assertEquals(REVIEWER_EMAIL, response.getReviewedByEmail());
        assertEquals(FundRequestStatus.SETTLEMENT_REVISION_REQUIRED, fundRequest.getStatus());
    }

    @Test
    void ownerCanResubmitRevisionRequiredSettlement() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_REVISION_REQUIRED);
        RequestSettlement settlement = settlement(
                fundRequest,
                RequestSettlementStatus.REVISION_REQUIRED,
                1,
                1);
        settlement.setLastRevisionNote("Perbaiki bukti transaksi");
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));
        stubSaves();

        SubmitSettlementRequest request = validSettlementRequest();
        request.setSpentAmount(new BigDecimal("1250000.00"));

        RequestSettlementResponse response = settlementService.resubmit(
                REQUEST_ID,
                request,
                owner);

        assertEquals(RequestSettlementStatus.SUBMITTED, response.getStatus());
        assertEquals(2, response.getSubmissionCount());
        assertEquals(1, response.getRevisionCount());
        assertEquals(new BigDecimal("250000.00"), response.getShortageAmount());
        assertEquals(FundRequestStatus.SETTLEMENT_SUBMITTED, fundRequest.getStatus());
    }

    @Test
    void reviewerCanApproveSubmittedSettlement() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(
                fundRequest,
                RequestSettlementStatus.SUBMITTED,
                1,
                0);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));
        stubSaves();

        RequestSettlementResponse response = settlementService.approve(
                REQUEST_ID,
                reviewer);

        assertEquals(RequestSettlementStatus.APPROVED, response.getStatus());
        assertEquals(REVIEWER_EMAIL, response.getReviewedByEmail());
        assertEquals(REVIEWER_EMAIL, response.getApprovedByEmail());
        assertNotNull(response.getReviewedAt());
        assertEquals(FundRequestStatus.COMPLETED, fundRequest.getStatus());
        assertNotNull(fundRequest.getCompletedAt());
    }

    @Test
    void unauthorizedUserCannotSubmitSettlement() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.FUND_RECEIVED);
        AuthenticatedUser anotherUser = new AuthenticatedUser(
                999L,
                "other@example.com",
                "Other User",
                List.of("ANGGOTA"));
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));

        assertThrows(
                ForbiddenException.class,
                () -> settlementService.submitFirst(
                        REQUEST_ID,
                        validSettlementRequest(),
                        anotherUser));

        verify(requestSettlementRepository, never()).save(any(RequestSettlement.class));
    }

    @Test
    void requesterCannotReviewOwnSettlement() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));

        RequestSettlementRevisionRequest request = new RequestSettlementRevisionRequest();
        request.setRevisionNote("Catatan revisi");

        assertThrows(
                ForbiddenException.class,
                () -> settlementService.requestRevision(REQUEST_ID, request, owner));

        verify(requestSettlementRepository, never())
                .findByFundRequestAndActiveTrue(any(FundRequest.class));
    }

    @Test
    void doubleApprovalCannotSucceedTwice() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(
                fundRequest,
                RequestSettlementStatus.SUBMITTED,
                1,
                0);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));
        stubSaves();

        settlementService.approve(REQUEST_ID, reviewer);

        assertThrows(
                BadRequestException.class,
                () -> settlementService.approve(REQUEST_ID, reviewer));

        verify(requestSettlementRepository, times(1)).save(settlement);
    }

    @Test
    void legacySettlementWithoutInternalStatusCanBeRead() {
        FundRequest fundRequest = fundRequest(FundRequestStatus.SETTLEMENT_SUBMITTED);
        RequestSettlement settlement = settlement(fundRequest, null, null, null);
        when(fundRequestRepository.findById(REQUEST_ID))
                .thenReturn(Optional.of(fundRequest));
        when(requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest))
                .thenReturn(Optional.of(settlement));

        RequestSettlementResponse response = settlementService.getSettlement(
                REQUEST_ID,
                owner,
                false);

        assertEquals(RequestSettlementStatus.SUBMITTED, response.getStatus());
        assertEquals(1, response.getSubmissionCount());
        assertEquals(0, response.getRevisionCount());
    }

    private FundRequest fundRequest(FundRequestStatus status) {
        return FundRequest.builder()
                .id(REQUEST_ID)
                .requesterAuthUserId(owner.userId())
                .requesterName(owner.fullName())
                .createdByEmail(OWNER_EMAIL)
                .updatedByEmail(OWNER_EMAIL)
                .totalAmount(new BigDecimal("1000000.00"))
                .status(status)
                .active(true)
                .build();
    }

    private RequestSettlement settlement(
            FundRequest fundRequest,
            RequestSettlementStatus status,
            Integer submissionCount,
            Integer revisionCount) {
        return RequestSettlement.builder()
                .id(10L)
                .fundRequest(fundRequest)
                .status(status)
                .spentAmount(new BigDecimal("900000.00"))
                .remainingAmount(new BigDecimal("100000.00"))
                .shortageAmount(BigDecimal.ZERO)
                .proofUrl("https://example.com/struk.jpg")
                .note("Penggunaan dana")
                .submissionCount(submissionCount)
                .revisionCount(revisionCount)
                .submittedByEmail(OWNER_EMAIL)
                .active(true)
                .build();
    }

    private SubmitSettlementRequest validSettlementRequest() {
        SubmitSettlementRequest request = new SubmitSettlementRequest();
        request.setSpentAmount(new BigDecimal("900000.00"));
        request.setProofUrl("https://example.com/struk.jpg");
        request.setNote("Penggunaan dana kegiatan");
        return request;
    }

    private void stubSaves() {
        when(requestSettlementRepository.save(any(RequestSettlement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(fundRequestRepository.save(any(FundRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(requestStatusHistoryRepository.save(any(RequestStatusHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
