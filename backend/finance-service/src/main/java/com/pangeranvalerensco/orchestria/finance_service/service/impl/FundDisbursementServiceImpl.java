package com.pangeranvalerensco.orchestria.finance_service.service.impl;

import com.pangeranvalerensco.orchestria.finance_service.entity.FundDisbursement;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import com.pangeranvalerensco.orchestria.finance_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.finance_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.finance_service.payload.request.CreateFundDisbursementRequest;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.FundDisbursementResponse;
import com.pangeranvalerensco.orchestria.finance_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.finance_service.repository.FundDisbursementRepository;
import com.pangeranvalerensco.orchestria.finance_service.service.FundDisbursementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pangeranvalerensco.orchestria.finance_service.client.RequestClient;
import com.pangeranvalerensco.orchestria.finance_service.client.dto.FundRequestSnapshotResponse;
import org.springframework.dao.DataIntegrityViolationException;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.RequestSyncStatus;
import com.pangeranvalerensco.orchestria.finance_service.exception.ExternalServiceException;
import com.pangeranvalerensco.orchestria.finance_service.exception.ForbiddenException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Objects;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FundDisbursementServiceImpl implements FundDisbursementService {

        private final FundDisbursementRepository fundDisbursementRepository;
        private final RequestClient requestClient;
        private final TransactionTemplate transactionTemplate;

        @Override
        public FundDisbursementResponse create(
                        CreateFundDisbursementRequest request,
                        String currentUserEmail,
                        String authorizationHeader) {
                if (fundDisbursementRepository
                                .existsByFundRequestIdAndActiveTrue(
                                                request.getFundRequestId())) {
                        throw new BadRequestException(
                                        "Pengajuan dana ini sudah pernah dicairkan");
                }

                FundRequestSnapshotResponse fundRequest = requestClient.getFundRequest(
                                request.getFundRequestId(),
                                authorizationHeader);

                if (fundRequest.getId() == null
                                || !fundRequest.getId().equals(
                                                request.getFundRequestId())) {
                        throw new BadRequestException(
                                        "ID pengajuan dari request-service tidak sesuai");
                }

                if (!Boolean.TRUE.equals(fundRequest.getActive())) {
                        throw new BadRequestException(
                                        "Pengajuan dana sudah tidak aktif");
                }

                if (!"READY_FOR_DISBURSEMENT".equals(
                                fundRequest.getStatus())) {
                        throw new BadRequestException(
                                        "Pengajuan hanya dapat dicairkan pada status READY_FOR_DISBURSEMENT");
                }

                if (fundRequest.getTotalAmount() == null
                                || fundRequest.getTotalAmount()
                                                .compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BadRequestException(
                                        "Nominal pengajuan dana tidak valid");
                }

                if (fundRequest.getTitle() == null
                                || fundRequest.getTitle().isBlank()
                                || fundRequest.getDivisionId() == null
                                || fundRequest.getDivisionName() == null
                                || fundRequest.getDivisionName().isBlank()
                                || fundRequest.getRequesterName() == null
                                || fundRequest.getRequesterName().isBlank()) {
                        throw new BadRequestException(
                                        "Data snapshot pengajuan tidak lengkap");
                }

                FundDisbursement disbursement = FundDisbursement.builder()
                                .fundRequestId(fundRequest.getId())
                                .requestTitle(fundRequest.getTitle())
                                .divisionId(fundRequest.getDivisionId())
                                .divisionName(fundRequest.getDivisionName())
                                .requesterName(fundRequest.getRequesterName())
                                .amount(fundRequest.getTotalAmount())
                                .method(request.getMethod())
                                .status(DisbursementStatus.DISBURSED)
                                .requestSyncStatus(RequestSyncStatus.PENDING)
                                .requestSyncAttempts(0)
                                .receiverName(request.getReceiverName())
                                .receiverNote(request.getReceiverNote())
                                .proofUrl(request.getProofUrl())
                                .note(request.getNote())
                                .disbursedByEmail(currentUserEmail)
                                .active(true)
                                .build();

                FundDisbursement saved = savePendingDisbursement(disbursement);

                return synchronizeRequestStatus(
                                saved.getId(),
                                authorizationHeader);
        }

        @Override
        @Transactional(readOnly = true)
        public FundDisbursementResponse getById(Long id) {
                FundDisbursement disbursement = fundDisbursementRepository.findById(id)
                                .filter(FundDisbursement::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Data pencairan dana tidak ditemukan"));

                return mapToResponse(disbursement);
        }

        @Override
        @Transactional(readOnly = true)
        public FundDisbursementResponse getByFundRequestId(Long fundRequestId) {
                FundDisbursement disbursement = fundDisbursementRepository
                                .findByFundRequestIdAndActiveTrue(fundRequestId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Data pencairan untuk pengajuan ini tidak ditemukan"));

                return mapToResponse(disbursement);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<FundDisbursementResponse> getAll(
                        DisbursementStatus status,
                        int page,
                        int size,
                        String sortBy,
                        String sortDirection) {
                int safePage = Math.max(page, 0);
                int safeSize = Math.min(Math.max(size, 1), 100);

                Sort sort = sortDirection.equalsIgnoreCase("asc")
                                ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(safePage, safeSize, sort);

                Page<FundDisbursement> pageResult = status == null
                                ? fundDisbursementRepository.findByActiveTrue(pageable)
                                : fundDisbursementRepository.findByActiveTrueAndStatus(status, pageable);

                return PageResponse.<FundDisbursementResponse>builder()
                                .content(pageResult.getContent().stream()
                                                .map(this::mapToResponse)
                                                .toList())
                                .page(pageResult.getNumber())
                                .size(pageResult.getSize())
                                .totalElements(pageResult.getTotalElements())
                                .totalPages(pageResult.getTotalPages())
                                .first(pageResult.isFirst())
                                .last(pageResult.isLast())
                                .build();
        }

        @Override
        public FundDisbursementResponse retryRequestSync(
                        Long id,
                        String authorizationHeader) {
                return synchronizeRequestStatus(
                                id,
                                authorizationHeader);
        }

        private FundDisbursement savePendingDisbursement(
                        FundDisbursement disbursement) {
                try {
                        return Objects.requireNonNull(
                                        transactionTemplate.execute(status -> fundDisbursementRepository.saveAndFlush(
                                                        disbursement)),
                                        "Penyimpanan pencairan menghasilkan null");

                } catch (DataIntegrityViolationException ex) {
                        throw new BadRequestException(
                                        "Pengajuan dana ini sudah pernah dicairkan");
                }
        }

        private FundDisbursementResponse synchronizeRequestStatus(
                        Long disbursementId,
                        String authorizationHeader) {
                FundDisbursement disbursement = findActiveDisbursement(disbursementId);

                if (disbursement.getRequestSyncStatus() == RequestSyncStatus.SYNCED) {
                        return mapToResponse(disbursement);
                }

                try {
                        FundRequestSnapshotResponse currentRequest = requestClient.getFundRequest(
                                        disbursement.getFundRequestId(),
                                        authorizationHeader);

                        if ("DISBURSED".equals(currentRequest.getStatus())) {
                                return markSyncSucceeded(disbursementId);
                        }

                        if (!"READY_FOR_DISBURSEMENT".equals(
                                        currentRequest.getStatus())) {
                                throw new BadRequestException(
                                                "Status pengajuan saat sinkronisasi adalah "
                                                                + currentRequest.getStatus()
                                                                + ", bukan READY_FOR_DISBURSEMENT");
                        }

                        FundRequestSnapshotResponse updatedRequest = requestClient.markDisbursed(
                                        disbursement.getFundRequestId(),
                                        authorizationHeader);

                        if (!"DISBURSED".equals(
                                        updatedRequest.getStatus())) {
                                throw new ExternalServiceException(
                                                "Request-service tidak mengembalikan status DISBURSED");
                        }

                        return markSyncSucceeded(disbursementId);

                } catch (RuntimeException ex) {
                        String message = "Pencairan tersimpan dengan ID "
                                        + disbursementId
                                        + ", tetapi sinkronisasi request-service gagal: "
                                        + safeErrorMessage(ex);

                        markSyncFailed(
                                        disbursementId,
                                        message);

                        if (ex instanceof ForbiddenException) {
                                throw new ForbiddenException(message);
                        }

                        if (ex instanceof BadRequestException) {
                                throw new BadRequestException(message);
                        }

                        if (ex instanceof ResourceNotFoundException) {
                                throw new ResourceNotFoundException(message);
                        }

                        throw new ExternalServiceException(message);
                }
        }

        private FundDisbursementResponse markSyncSucceeded(
                        Long disbursementId) {
                FundDisbursement updated = Objects.requireNonNull(
                                transactionTemplate.execute(status -> {
                                        FundDisbursement entity = findActiveDisbursement(
                                                        disbursementId);

                                        entity.setRequestSyncStatus(
                                                        RequestSyncStatus.SYNCED);
                                        entity.setRequestSyncError(null);
                                        entity.setRequestSyncedAt(
                                                        LocalDateTime.now());
                                        entity.setRequestSyncAttempts(
                                                        nextSyncAttempt(entity));

                                        return fundDisbursementRepository
                                                        .saveAndFlush(entity);
                                }));

                return mapToResponse(updated);
        }

        private void markSyncFailed(
                        Long disbursementId,
                        String errorMessage) {
                transactionTemplate.executeWithoutResult(status -> {
                        FundDisbursement entity = findActiveDisbursement(
                                        disbursementId);

                        entity.setRequestSyncStatus(
                                        RequestSyncStatus.FAILED);
                        entity.setRequestSyncError(
                                        truncate(errorMessage, 500));
                        entity.setRequestSyncAttempts(
                                        nextSyncAttempt(entity));

                        fundDisbursementRepository.saveAndFlush(entity);
                });
        }

        private FundDisbursement findActiveDisbursement(
                        Long id) {
                return fundDisbursementRepository.findById(id)
                                .filter(FundDisbursement::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Data pencairan dana tidak ditemukan"));
        }

        private int nextSyncAttempt(
                        FundDisbursement disbursement) {
                Integer attempts = disbursement.getRequestSyncAttempts();

                return attempts == null ? 1 : attempts + 1;
        }

        private String safeErrorMessage(Throwable throwable) {
                String message = throwable.getMessage();

                return message == null || message.isBlank()
                                ? throwable.getClass().getSimpleName()
                                : message;
        }

        private String truncate(
                        String value,
                        int maximumLength) {
                if (value == null
                                || value.length() <= maximumLength) {
                        return value;
                }

                return value.substring(0, maximumLength);
        }

        private FundDisbursementResponse mapToResponse(FundDisbursement disbursement) {
                return FundDisbursementResponse.builder()
                                .id(disbursement.getId())
                                .fundRequestId(disbursement.getFundRequestId())
                                .requestTitle(disbursement.getRequestTitle())
                                .divisionId(disbursement.getDivisionId())
                                .divisionName(disbursement.getDivisionName())
                                .requesterName(disbursement.getRequesterName())
                                .amount(disbursement.getAmount())
                                .method(disbursement.getMethod())
                                .status(disbursement.getStatus())
                                .requestSyncStatus(disbursement.getRequestSyncStatus())
                                .requestSyncError(disbursement.getRequestSyncError())
                                .requestSyncedAt(disbursement.getRequestSyncedAt())
                                .requestSyncAttempts(disbursement.getRequestSyncAttempts())
                                .receiverName(disbursement.getReceiverName())
                                .receiverNote(disbursement.getReceiverNote())
                                .proofUrl(disbursement.getProofUrl())
                                .note(disbursement.getNote())
                                .disbursedByEmail(disbursement.getDisbursedByEmail())
                                .disbursedAt(disbursement.getDisbursedAt())
                                .active(disbursement.getActive())
                                .createdAt(disbursement.getCreatedAt())
                                .updatedAt(disbursement.getUpdatedAt())
                                .build();
        }
}