package com.pangeranvalerensco.orchestria.request_service.service;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestSettlementStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.RequestSettlementRevisionRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestSettlementRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final FundRequestRepository fundRequestRepository;
    private final RequestSettlementRepository requestSettlementRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;

    @Transactional(readOnly = true)
    public RequestSettlementResponse getSettlement(
            Long requestId,
            AuthenticatedUser currentUser,
            boolean canVerifySettlement) {
        FundRequest fundRequest = findActiveRequest(requestId);

        if (!canVerifySettlement) {
            validateOwner(fundRequest, currentUser);
        }

        RequestSettlement settlement = findActiveSettlement(fundRequest);
        return mapToResponse(settlement);
    }

    @Transactional
    public RequestSettlementResponse submitFirst(
            Long requestId,
            SubmitSettlementRequest request,
            AuthenticatedUser currentUser) {
        FundRequest fundRequest = findActiveRequest(requestId);
        validateOwner(fundRequest, currentUser);

        if (fundRequest.getStatus() != FundRequestStatus.FUND_RECEIVED) {
            throw new BadRequestException(
                    "Settlement pertama hanya dapat dikirim ketika status pengajuan FUND_RECEIVED");
        }

        if (requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest)) {
            throw new BadRequestException("Settlement untuk pengajuan ini sudah tersedia");
        }

        SettlementValues values = validateAndCalculate(fundRequest, request);
        LocalDateTime now = LocalDateTime.now();

        RequestSettlement settlement = RequestSettlement.builder()
                .fundRequest(fundRequest)
                .status(RequestSettlementStatus.SUBMITTED)
                .spentAmount(values.spentAmount())
                .remainingAmount(values.remainingAmount())
                .shortageAmount(values.shortageAmount())
                .proofUrl(values.proofUrl())
                .note(values.note())
                .submissionCount(1)
                .revisionCount(0)
                .submittedByEmail(currentUser.email())
                .submittedAt(now)
                .active(true)
                .build();

        RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);
        transitionRequest(
                fundRequest,
                FundRequestStatus.SETTLEMENT_SUBMITTED,
                currentUser.email(),
                "Settlement penggunaan dana dikirim");

        return mapToResponse(savedSettlement);
    }

    @Transactional
    public RequestSettlementResponse resubmit(
            Long requestId,
            SubmitSettlementRequest request,
            AuthenticatedUser currentUser) {
        FundRequest fundRequest = findActiveRequest(requestId);
        validateOwner(fundRequest, currentUser);

        if (fundRequest.getStatus() != FundRequestStatus.SETTLEMENT_REVISION_REQUIRED) {
            throw new BadRequestException(
                    "Settlement hanya dapat dikirim ulang ketika status pengajuan SETTLEMENT_REVISION_REQUIRED");
        }

        RequestSettlement settlement = findActiveSettlement(fundRequest);
        if (resolveStatus(settlement) != RequestSettlementStatus.REVISION_REQUIRED) {
            throw new BadRequestException(
                    "Hanya settlement berstatus REVISION_REQUIRED yang dapat diperbarui dan dikirim ulang");
        }

        SettlementValues values = validateAndCalculate(fundRequest, request);
        settlement.setSpentAmount(values.spentAmount());
        settlement.setRemainingAmount(values.remainingAmount());
        settlement.setShortageAmount(values.shortageAmount());
        settlement.setProofUrl(values.proofUrl());
        settlement.setNote(values.note());
        settlement.setStatus(RequestSettlementStatus.SUBMITTED);
        settlement.setSubmissionCount(currentSubmissionCount(settlement) + 1);
        settlement.setSubmittedByEmail(currentUser.email());
        settlement.setSubmittedAt(LocalDateTime.now());

        RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);
        transitionRequest(
                fundRequest,
                FundRequestStatus.SETTLEMENT_SUBMITTED,
                currentUser.email(),
                "Settlement penggunaan dana diperbaiki dan dikirim ulang");

        return mapToResponse(savedSettlement);
    }

    @Transactional
    public RequestSettlementResponse requestRevision(
            Long requestId,
            RequestSettlementRevisionRequest request,
            AuthenticatedUser reviewer) {
        FundRequest fundRequest = findActiveRequest(requestId);
        validateReviewerIsNotOwner(fundRequest, reviewer);

        if (fundRequest.getStatus() != FundRequestStatus.SETTLEMENT_SUBMITTED) {
            throw new BadRequestException(
                    "Revisi hanya dapat diminta ketika status pengajuan SETTLEMENT_SUBMITTED");
        }

        RequestSettlement settlement = findActiveSettlement(fundRequest);
        if (resolveStatus(settlement) != RequestSettlementStatus.SUBMITTED) {
            throw new BadRequestException(
                    "Revisi hanya dapat diminta untuk settlement berstatus SUBMITTED");
        }

        String revisionNote = normalizeRevisionNote(request.getRevisionNote());
        LocalDateTime now = LocalDateTime.now();

        settlement.setStatus(RequestSettlementStatus.REVISION_REQUIRED);
        settlement.setRevisionCount(currentRevisionCount(settlement) + 1);
        settlement.setLastRevisionNote(revisionNote);
        settlement.setReviewedByEmail(reviewer.email());
        settlement.setReviewedAt(now);

        RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);
        transitionRequest(
                fundRequest,
                FundRequestStatus.SETTLEMENT_REVISION_REQUIRED,
                reviewer.email(),
                "Settlement membutuhkan revisi: " + revisionNote);

        return mapToResponse(savedSettlement);
    }

    @Transactional
    public RequestSettlementResponse approve(
            Long requestId,
            AuthenticatedUser reviewer) {
        FundRequest fundRequest = findActiveRequest(requestId);
        validateReviewerIsNotOwner(fundRequest, reviewer);

        if (fundRequest.getStatus() != FundRequestStatus.SETTLEMENT_SUBMITTED) {
            throw new BadRequestException(
                    "Settlement hanya dapat disetujui ketika status pengajuan SETTLEMENT_SUBMITTED");
        }

        RequestSettlement settlement = findActiveSettlement(fundRequest);
        if (resolveStatus(settlement) != RequestSettlementStatus.SUBMITTED) {
            throw new BadRequestException(
                    "Settlement hanya dapat disetujui ketika berstatus SUBMITTED");
        }

        settlement.setProofUrl(validateProofUrl(settlement.getProofUrl()));
        LocalDateTime now = LocalDateTime.now();
        settlement.setStatus(RequestSettlementStatus.APPROVED);
        settlement.setReviewedByEmail(reviewer.email());
        settlement.setReviewedAt(now);
        settlement.setApprovedByEmail(reviewer.email());
        settlement.setApprovedAt(now);

        RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);
        fundRequest.setCompletedAt(now);
        transitionRequest(
                fundRequest,
                FundRequestStatus.COMPLETED,
                reviewer.email(),
                "Settlement disetujui dan pengajuan selesai");

        return mapToResponse(savedSettlement);
    }

    private FundRequest findActiveRequest(Long requestId) {
        return fundRequestRepository.findById(requestId)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pengajuan dana tidak ditemukan"));
    }

    private RequestSettlement findActiveSettlement(FundRequest fundRequest) {
        return requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Settlement pengajuan tidak ditemukan"));
    }

    private void validateOwner(FundRequest fundRequest, AuthenticatedUser currentUser) {
        if (!isOwner(fundRequest, currentUser)) {
            throw new ForbiddenException(
                    "Anda tidak memiliki akses untuk memproses settlement pengajuan ini");
        }
    }

    private void validateReviewerIsNotOwner(
            FundRequest fundRequest,
            AuthenticatedUser reviewer) {
        validateAuthenticatedUser(reviewer);
        if (isOwner(fundRequest, reviewer)) {
            throw new ForbiddenException(
                    "Pemohon tidak boleh meninjau settlement miliknya sendiri");
        }
    }

    private boolean isOwner(FundRequest fundRequest, AuthenticatedUser currentUser) {
        validateAuthenticatedUser(currentUser);

        if (fundRequest.getRequesterAuthUserId() != null
                && currentUser.userId() != null) {
            return fundRequest.getRequesterAuthUserId().equals(currentUser.userId());
        }

        return fundRequest.getCreatedByEmail() != null
                && fundRequest.getCreatedByEmail().equalsIgnoreCase(currentUser.email());
    }

    private void validateAuthenticatedUser(AuthenticatedUser currentUser) {
        if (currentUser == null
                || currentUser.email() == null
                || currentUser.email().isBlank()) {
            throw new ForbiddenException("Identitas user login tidak valid");
        }
    }

    private SettlementValues validateAndCalculate(
            FundRequest fundRequest,
            SubmitSettlementRequest request) {
        if (request == null
                || request.getSpentAmount() == null
                || request.getSpentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Nominal penggunaan harus lebih dari 0");
        }

        BigDecimal requestedAmount = fundRequest.getTotalAmount();
        if (requestedAmount == null
                || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Total pengajuan tidak valid");
        }

        BigDecimal spentAmount = request.getSpentAmount();
        BigDecimal remainingAmount = BigDecimal.ZERO;
        BigDecimal shortageAmount = BigDecimal.ZERO;

        if (spentAmount.compareTo(requestedAmount) < 0) {
            remainingAmount = requestedAmount.subtract(spentAmount);
        } else if (spentAmount.compareTo(requestedAmount) > 0) {
            shortageAmount = spentAmount.subtract(requestedAmount);
        }

        return new SettlementValues(
                spentAmount,
                remainingAmount,
                shortageAmount,
                validateProofUrl(request.getProofUrl()),
                normalizeOptionalText(request.getNote()));
    }

    private String validateProofUrl(String proofUrl) {
        if (proofUrl == null || proofUrl.isBlank()) {
            throw new BadRequestException("Bukti penggunaan dana wajib diisi");
        }

        String normalized = proofUrl.trim();
        try {
            URI uri = URI.create(normalized);
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || uri.getHost().isBlank()) {
                throw new BadRequestException(
                        "Bukti penggunaan dana harus menggunakan URL HTTPS yang valid");
            }
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException(
                    "Bukti penggunaan dana harus menggunakan URL HTTPS yang valid");
        }

        return normalized;
    }

    private String normalizeRevisionNote(String revisionNote) {
        if (revisionNote == null || revisionNote.isBlank()) {
            throw new BadRequestException("Catatan revisi wajib diisi");
        }
        return revisionNote.trim();
    }

    private String normalizeOptionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int currentSubmissionCount(RequestSettlement settlement) {
        return settlement.getSubmissionCount() == null
                ? 1
                : settlement.getSubmissionCount();
    }

    private int currentRevisionCount(RequestSettlement settlement) {
        return settlement.getRevisionCount() == null
                ? 0
                : settlement.getRevisionCount();
    }

    private RequestSettlementStatus resolveStatus(RequestSettlement settlement) {
        if (settlement.getStatus() != null) {
            return settlement.getStatus();
        }

        FundRequestStatus requestStatus = settlement.getFundRequest().getStatus();
        if (requestStatus == FundRequestStatus.COMPLETED
                || settlement.getApprovedAt() != null) {
            return RequestSettlementStatus.APPROVED;
        }
        if (requestStatus == FundRequestStatus.SETTLEMENT_REVISION_REQUIRED) {
            return RequestSettlementStatus.REVISION_REQUIRED;
        }
        return RequestSettlementStatus.SUBMITTED;
    }

    private void transitionRequest(
            FundRequest fundRequest,
            FundRequestStatus newStatus,
            String changedByEmail,
            String note) {
        FundRequestStatus oldStatus = fundRequest.getStatus();
        fundRequest.setStatus(newStatus);
        fundRequest.setUpdatedByEmail(changedByEmail);

        FundRequest savedRequest = fundRequestRepository.save(fundRequest);
        requestStatusHistoryRepository.save(
                RequestStatusHistory.builder()
                        .fundRequest(savedRequest)
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .changedByEmail(changedByEmail)
                        .note(note)
                        .build());
    }

    private RequestSettlementResponse mapToResponse(RequestSettlement settlement) {
        FundRequest fundRequest = settlement.getFundRequest();
        return RequestSettlementResponse.builder()
                .id(settlement.getId())
                .fundRequestId(fundRequest.getId())
                .status(resolveStatus(settlement))
                .requestedAmount(fundRequest.getTotalAmount())
                .spentAmount(settlement.getSpentAmount())
                .remainingAmount(settlement.getRemainingAmount())
                .shortageAmount(settlement.getShortageAmount())
                .proofUrl(settlement.getProofUrl())
                .note(settlement.getNote())
                .submissionCount(currentSubmissionCount(settlement))
                .revisionCount(currentRevisionCount(settlement))
                .lastRevisionNote(settlement.getLastRevisionNote())
                .reviewedByEmail(settlement.getReviewedByEmail())
                .reviewedAt(settlement.getReviewedAt())
                .submittedByEmail(settlement.getSubmittedByEmail())
                .submittedAt(settlement.getSubmittedAt())
                .approvedByEmail(settlement.getApprovedByEmail())
                .approvedAt(settlement.getApprovedAt())
                .lockVersion(settlement.getLockVersion())
                .active(settlement.getActive())
                .createdAt(settlement.getCreatedAt())
                .updatedAt(settlement.getUpdatedAt())
                .build();
    }

    private record SettlementValues(
            BigDecimal spentAmount,
            BigDecimal remainingAmount,
            BigDecimal shortageAmount,
            String proofUrl,
            String note) {
    }
}
