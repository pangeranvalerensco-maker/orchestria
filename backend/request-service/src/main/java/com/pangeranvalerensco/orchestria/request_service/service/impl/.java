package com.pangeranvalerensco.orchestria.request_service.service.impl;

import com.pangeranvalerensco.orchestria.request_service.client.OrganizationClient;
import com.pangeranvalerensco.orchestria.request_service.client.dto.OrganizationMemberContextResponse;
import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestApproval;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.ProcessApprovalRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestApprovalRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;
import com.pangeranvalerensco.orchestria.request_service.service.FundRequestService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestApprovalServiceImplTest {

    private static final String AUTHORIZATION_HEADER =
            "Bearer test-token";

    @Mock
    private FundRequestRepository fundRequestRepository;

    @Mock
    private RequestApprovalRepository requestApprovalRepository;

    @Mock
    private RequestStatusHistoryRepository
            requestStatusHistoryRepository;

    @Mock
    private FundRequestService fundRequestService;

    @Mock
    private OrganizationClient organizationClient;

    @InjectMocks
    private RequestApprovalServiceImpl requestApprovalService;

    private AuthenticatedUser divisionHead;

    @BeforeEach
    void setUp() {
        divisionHead = new AuthenticatedUser(
                7L,
                "ketua.divisi@example.com",
                "Ketua Divisi",
                List.of("KETUA_DIVISI")
        );
    }

    @Test
    void divisionHeadCannotApproveRequestFromAnotherDivision() {
        FundRequest fundRequest = createFundRequest(1L, 10L);

        when(fundRequestRepository.findById(1L))
                .thenReturn(Optional.of(fundRequest));

        when(organizationClient.getCurrentMemberContext(
                AUTHORIZATION_HEADER
        )).thenReturn(
                createOrganizationContext(
                        7L,
                        "ketua.divisi@example.com",
                        20L,
                        "KETUA_DIVISI"
                )
        );

        ProcessApprovalRequest approvalRequest =
                createApprovalRequest();

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> requestApprovalService.approve(
                        1L,
                        approvalRequest,
                        divisionHead,
                        AUTHORIZATION_HEADER
                )
        );

        assertEquals(
                "Anda bukan Ketua Divisi aktif pada divisi pengajuan ini",
                exception.getMessage()
        );

        assertEquals(
                FundRequestStatus.SUBMITTED,
                fundRequest.getStatus()
        );

        verify(fundRequestRepository, never())
                .save(any(FundRequest.class));

        verifyNoInteractions(
                requestApprovalRepository,
                requestStatusHistoryRepository,
                fundRequestService
        );
    }

    @Test
    void divisionHeadCanApproveRequestFromOwnDivision() {
        FundRequest fundRequest = createFundRequest(1L, 10L);

        when(fundRequestRepository.findById(1L))
                .thenReturn(Optional.of(fundRequest));

        when(organizationClient.getCurrentMemberContext(
                AUTHORIZATION_HEADER
        )).thenReturn(
                createOrganizationContext(
                        7L,
                        "ketua.divisi@example.com",
                        10L,
                        "KETUA_DIVISI"
                )
        );

        when(fundRequestRepository.save(
                any(FundRequest.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        FundRequestResponse expectedResponse =
                FundRequestResponse.builder()
                        .id(1L)
                        .divisionId(10L)
                        .status(
                                FundRequestStatus.DIVISION_APPROVED
                        )
                        .build();

        when(fundRequestService.getById(1L))
                .thenReturn(expectedResponse);

        FundRequestResponse result =
                requestApprovalService.approve(
                        1L,
                        createApprovalRequest(),
                        divisionHead,
                        AUTHORIZATION_HEADER
                );

        assertSame(expectedResponse, result);

        assertEquals(
                FundRequestStatus.DIVISION_APPROVED,
                fundRequest.getStatus()
        );

        assertEquals(
                "ketua.divisi@example.com",
                fundRequest.getUpdatedByEmail()
        );

        verify(requestApprovalRepository)
                .save(any(RequestApproval.class));

        verify(requestStatusHistoryRepository)
                .save(any(RequestStatusHistory.class));

        verify(fundRequestRepository)
                .save(fundRequest);
    }

    @Test
    void superAdminCanApproveWithoutOrganizationAssignmentLookup() {
        FundRequest fundRequest = createFundRequest(1L, 10L);

        AuthenticatedUser superAdmin =
                new AuthenticatedUser(
                        1L,
                        "admin@example.com",
                        "Super Admin",
                        List.of("SUPER_ADMIN")
                );

        when(fundRequestRepository.findById(1L))
                .thenReturn(Optional.of(fundRequest));

        when(fundRequestRepository.save(
                any(FundRequest.class)
        )).thenAnswer(invocation ->
                invocation.getArgument(0)
        );

        FundRequestResponse expectedResponse =
                FundRequestResponse.builder()
                        .id(1L)
                        .divisionId(10L)
                        .status(
                                FundRequestStatus.DIVISION_APPROVED
                        )
                        .build();

        when(fundRequestService.getById(1L))
                .thenReturn(expectedResponse);

        FundRequestResponse result =
                requestApprovalService.approve(
                        1L,
                        createApprovalRequest(),
                        superAdmin,
                        AUTHORIZATION_HEADER
                );

        assertSame(expectedResponse, result);

        assertEquals(
                FundRequestStatus.DIVISION_APPROVED,
                fundRequest.getStatus()
        );

        verifyNoInteractions(organizationClient);
    }

    private FundRequest createFundRequest(
            Long id,
            Long divisionId
    ) {
        return FundRequest.builder()
                .id(id)
                .divisionId(divisionId)
                .status(FundRequestStatus.SUBMITTED)
                .active(true)
                .build();
    }

    private ProcessApprovalRequest createApprovalRequest() {
        ProcessApprovalRequest request =
                new ProcessApprovalRequest();

        request.setLevel(ApprovalLevel.DIVISION);
        request.setNote("Disetujui");

        return request;
    }

    private OrganizationMemberContextResponse
    createOrganizationContext(
            Long authUserId,
            String email,
            Long divisionId,
            String positionCode
    ) {
        OrganizationMemberContextResponse.MemberData member =
                new OrganizationMemberContextResponse.MemberData(
                        100L,
                        authUserId,
                        "Ketua Divisi",
                        email,
                        true
                );

        OrganizationMemberContextResponse.AssignmentData assignment =
                new OrganizationMemberContextResponse.AssignmentData(
                        divisionId,
                        "DIV-" + divisionId,
                        "Divisi " + divisionId,
                        positionCode,
                        "Ketua Divisi",
                        true
                );

        return new OrganizationMemberContextResponse(
                member,
                List.of(assignment)
        );
    }
}
