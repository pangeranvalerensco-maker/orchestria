package com.pangeranvalerensco.orchestria.finance_service.service.impl;

import com.pangeranvalerensco.orchestria.finance_service.client.RequestClient;
import com.pangeranvalerensco.orchestria.finance_service.client.dto.FundRequestSnapshotResponse;
import com.pangeranvalerensco.orchestria.finance_service.entity.FundDisbursement;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementMethod;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.RequestSyncStatus;
import com.pangeranvalerensco.orchestria.finance_service.exception.ExternalServiceException;
import com.pangeranvalerensco.orchestria.finance_service.payload.request.CreateFundDisbursementRequest;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.FundDisbursementResponse;
import com.pangeranvalerensco.orchestria.finance_service.repository.FundDisbursementRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FundDisbursementServiceImplTest {

    private static final Long FUND_REQUEST_ID = 42L;
    private static final Long DISBURSEMENT_ID = 10L;
    private static final String AUTHORIZATION_HEADER =
            "Bearer test-token";

    @Mock
    private FundDisbursementRepository
            fundDisbursementRepository;

    @Mock
    private RequestClient requestClient;

    private FundDisbursementServiceImpl
            fundDisbursementService;

    private AtomicReference<FundDisbursement> storedEntity;

    @BeforeEach
    void setUp() {
        storedEntity = new AtomicReference<>();

        when(fundDisbursementRepository.saveAndFlush(
                any(FundDisbursement.class)
        )).thenAnswer(invocation -> {
            FundDisbursement entity =
                    invocation.getArgument(0);

            if (entity.getId() == null) {
                entity.setId(DISBURSEMENT_ID);
            }

            storedEntity.set(entity);
            return entity;
        });

        when(fundDisbursementRepository.findById(anyLong()))
                .thenAnswer(invocation -> {
                    Long requestedId =
                            invocation.getArgument(0);

                    FundDisbursement entity =
                            storedEntity.get();

                    if (entity != null
                            && Objects.equals(
                                    entity.getId(),
                                    requestedId
                            )) {
                        return Optional.of(entity);
                    }

                    return Optional.empty();
                });

        TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                        new NoOpTransactionManager()
                );

        fundDisbursementService =
                new FundDisbursementServiceImpl(
                        fundDisbursementRepository,
                        requestClient,
                        transactionTemplate
                );
    }

    @Test
    void createMarksSynchronizationAsSynced() {
        when(fundDisbursementRepository
                .existsByFundRequestIdAndActiveTrue(
                        FUND_REQUEST_ID
                ))
                .thenReturn(false);

        when(requestClient.getFundRequest(
                FUND_REQUEST_ID,
                AUTHORIZATION_HEADER
        ))
                .thenReturn(
                        snapshot("READY_FOR_DISBURSEMENT"),
                        snapshot("READY_FOR_DISBURSEMENT")
                );

        when(requestClient.markDisbursed(
                FUND_REQUEST_ID,
                AUTHORIZATION_HEADER
        ))
                .thenReturn(snapshot("DISBURSED"));

        FundDisbursementResponse result =
                fundDisbursementService.create(
                        createRequest(),
                        "bendahara@example.com",
                        AUTHORIZATION_HEADER
                );

        assertEquals(
                RequestSyncStatus.SYNCED,
                result.getRequestSyncStatus()
        );

        assertEquals(
                1,
                result.getRequestSyncAttempts()
        );

        assertNotNull(result.getRequestSyncedAt());

        verify(requestClient, times(2))
                .getFundRequest(
                        FUND_REQUEST_ID,
                        AUTHORIZATION_HEADER
                );

        verify(requestClient)
                .markDisbursed(
                        FUND_REQUEST_ID,
                        AUTHORIZATION_HEADER
                );
    }

    @Test
    void createStoresFailedStatusWhenRequestServiceFails() {
        when(fundDisbursementRepository
                .existsByFundRequestIdAndActiveTrue(
                        FUND_REQUEST_ID
                ))
                .thenReturn(false);

        when(requestClient.getFundRequest(
                FUND_REQUEST_ID,
                AUTHORIZATION_HEADER
        ))
                .thenReturn(snapshot("READY_FOR_DISBURSEMENT"))
                .thenThrow(
                        new ExternalServiceException(
                                "Request-service tidak dapat diakses"
                        )
                );

        ExternalServiceException exception =
                assertThrows(
                        ExternalServiceException.class,
                        () -> fundDisbursementService.create(
                                createRequest(),
                                "bendahara@example.com",
                                AUTHORIZATION_HEADER
                        )
                );

        FundDisbursement stored =
                storedEntity.get();

        assertNotNull(stored);

        assertEquals(
                RequestSyncStatus.FAILED,
                stored.getRequestSyncStatus()
        );

        assertEquals(
                1,
                stored.getRequestSyncAttempts()
        );

        assertNotNull(stored.getRequestSyncError());

        assertEquals(
                true,
                exception.getMessage()
                        .contains(
                                "sinkronisasi request-service gagal"
                        )
        );
    }

    @Test
    void retryDoesNotMarkDisbursedAgainWhenAlreadyDisbursed() {
        FundDisbursement failedDisbursement =
                FundDisbursement.builder()
                        .id(DISBURSEMENT_ID)
                        .fundRequestId(FUND_REQUEST_ID)
                        .requestTitle("Kegiatan PUB")
                        .divisionId(7L)
                        .divisionName("Divisi Pendidikan")
                        .requesterName("Pemohon")
                        .amount(new BigDecimal("150000"))
                        .method(
                                DisbursementMethod.BANK_TRANSFER
                        )
                        .status(
                                DisbursementStatus.DISBURSED
                        )
                        .requestSyncStatus(
                                RequestSyncStatus.FAILED
                        )
                        .requestSyncAttempts(1)
                        .receiverName("Penerima")
                        .disbursedByEmail(
                                "bendahara@example.com"
                        )
                        .active(true)
                        .build();

        storedEntity.set(failedDisbursement);

        when(requestClient.getFundRequest(
                FUND_REQUEST_ID,
                AUTHORIZATION_HEADER
        ))
                .thenReturn(snapshot("DISBURSED"));

        FundDisbursementResponse result =
                fundDisbursementService.retryRequestSync(
                        DISBURSEMENT_ID,
                        AUTHORIZATION_HEADER
                );

        assertEquals(
                RequestSyncStatus.SYNCED,
                result.getRequestSyncStatus()
        );

        assertEquals(
                2,
                result.getRequestSyncAttempts()
        );

        assertNotNull(result.getRequestSyncedAt());

        verify(requestClient, never())
                .markDisbursed(
                        FUND_REQUEST_ID,
                        AUTHORIZATION_HEADER
                );
    }

    private CreateFundDisbursementRequest createRequest() {
        CreateFundDisbursementRequest request =
                new CreateFundDisbursementRequest();

        request.setFundRequestId(FUND_REQUEST_ID);
        request.setMethod(
                DisbursementMethod.BANK_TRANSFER
        );
        request.setReceiverName("Penerima Dana");
        request.setReceiverNote("Rekening organisasi");
        request.setProofUrl(
                "https://example.com/proof.jpg"
        );
        request.setNote("Pencairan dana kegiatan");

        return request;
    }

    private FundRequestSnapshotResponse snapshot(
            String status
    ) {
        return new FundRequestSnapshotResponse(
                FUND_REQUEST_ID,
                7L,
                "Divisi Pendidikan",
                99L,
                "Pemohon",
                "Kegiatan PUB",
                status,
                new BigDecimal("150000"),
                true
        );
    }

    private static class NoOpTransactionManager
            implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(
                TransactionDefinition definition
        ) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            // Tidak ada database nyata pada unit test.
        }

        @Override
        public void rollback(TransactionStatus status) {
            // Tidak ada database nyata pada unit test.
        }
    }
}