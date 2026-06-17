package com.pangeranvalerensco.orchestria.request_service.service.impl;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestApproval;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalDecision;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.ProcessApprovalRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestApprovalRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.service.FundRequestService;
import com.pangeranvalerensco.orchestria.request_service.service.RequestApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RequestApprovalServiceImpl implements RequestApprovalService {

    private final FundRequestRepository fundRequestRepository;
    private final RequestApprovalRepository requestApprovalRepository;
    private final RequestStatusHistoryRepository requestStatusHistoryRepository;
    private final FundRequestService fundRequestService;

    @Override
    @Transactional
    public FundRequestResponse approve(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail) {
        FundRequest fundRequest = findActiveFundRequest(fundRequestId);

        FundRequestStatus oldStatus = fundRequest.getStatus();
        FundRequestStatus newStatus = determineNextApprovedStatus(oldStatus, request.getLevel());

        saveApproval(
                fundRequest,
                request,
                currentUserEmail,
                ApprovalDecision.APPROVED);

        fundRequest.setStatus(newStatus);
        fundRequest.setUpdatedByEmail(currentUserEmail);

        FundRequest saved = fundRequestRepository.save(fundRequest);

        saveStatusHistory(
                saved,
                oldStatus,
                newStatus,
                currentUserEmail,
                "Pengajuan disetujui pada level " + request.getLevel());

        return fundRequestService.getById(saved.getId());
    }

    @Override
    @Transactional
    public FundRequestResponse reject(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail) {
        FundRequest fundRequest = findActiveFundRequest(fundRequestId);

        validateApprovalStage(
                fundRequest.getStatus(),
                request.getLevel());

        FundRequestStatus oldStatus = fundRequest.getStatus();
        FundRequestStatus newStatus = FundRequestStatus.REJECTED;

        saveApproval(
                fundRequest,
                request,
                currentUserEmail,
                ApprovalDecision.REJECTED);

        fundRequest.setStatus(newStatus);
        fundRequest.setUpdatedByEmail(currentUserEmail);

        FundRequest saved = fundRequestRepository.save(fundRequest);

        saveStatusHistory(
                saved,
                oldStatus,
                newStatus,
                currentUserEmail,
                request.getNote() == null ? "Pengajuan ditolak" : request.getNote());

        return fundRequestService.getById(saved.getId());
    }

    @Override
    @Transactional
    public FundRequestResponse requestRevision(
            Long fundRequestId,
            ProcessApprovalRequest request,
            String currentUserEmail) {
        FundRequest fundRequest = findActiveFundRequest(fundRequestId);

        validateApprovalStage(fundRequest.getStatus(), request.getLevel());

        FundRequestStatus oldStatus = fundRequest.getStatus();
        FundRequestStatus newStatus = FundRequestStatus.REVISION_REQUESTED;

        saveApproval(
                fundRequest,
                request,
                currentUserEmail,
                ApprovalDecision.REVISION_REQUESTED);

        fundRequest.setStatus(newStatus);
        fundRequest.setUpdatedByEmail(currentUserEmail);

        FundRequest saved = fundRequestRepository.save(fundRequest);

        saveStatusHistory(
                saved,
                oldStatus,
                newStatus,
                currentUserEmail,
                request.getNote() == null ? "Pengajuan diminta revisi" : request.getNote());

        return fundRequestService.getById(saved.getId());
    }

    private FundRequest findActiveFundRequest(Long fundRequestId) {
        return fundRequestRepository.findById(fundRequestId)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));
    }

    private FundRequestStatus determineNextApprovedStatus(
            FundRequestStatus currentStatus,
            ApprovalLevel approvalLevel) {
        if (approvalLevel == ApprovalLevel.DIVISION) {
            if (currentStatus != FundRequestStatus.SUBMITTED) {
                throw new BadRequestException("Approval DIVISION hanya bisa dilakukan pada status SUBMITTED");
            }

            return FundRequestStatus.DIVISION_APPROVED;
        }

        if (approvalLevel == ApprovalLevel.PUB) {
            if (currentStatus != FundRequestStatus.DIVISION_APPROVED) {
                throw new BadRequestException("Approval PUB hanya bisa dilakukan setelah DIVISION_APPROVED");
            }

            return FundRequestStatus.PUB_APPROVED;
        }

        if (approvalLevel == ApprovalLevel.PEMBINA) {
            if (currentStatus != FundRequestStatus.PUB_APPROVED) {
                throw new BadRequestException("Approval PEMBINA hanya bisa dilakukan setelah PUB_APPROVED");
            }

            return FundRequestStatus.READY_FOR_DISBURSEMENT;
        }

        throw new BadRequestException("Level approval tidak valid");
    }

    private void validateApprovalStage(
            FundRequestStatus currentStatus,
            ApprovalLevel requestedLevel) {
        ApprovalLevel expectedLevel;

        if (currentStatus == FundRequestStatus.SUBMITTED) {
            expectedLevel = ApprovalLevel.DIVISION;
        } else if (currentStatus == FundRequestStatus.DIVISION_APPROVED) {
            expectedLevel = ApprovalLevel.PUB;
        } else if (currentStatus == FundRequestStatus.PUB_APPROVED) {
            expectedLevel = ApprovalLevel.PEMBINA;
        } else {
            throw new BadRequestException(
                    "Pengajuan tidak sedang berada dalam proses approval");
        }

        if (requestedLevel != expectedLevel) {
            throw new BadRequestException(
                    "Tahap approval saat ini adalah "
                            + expectedLevel
                            + ", bukan "
                            + requestedLevel);
        }
    }

    private void saveApproval(
            FundRequest fundRequest,
            ProcessApprovalRequest request,
            String currentUserEmail,
            ApprovalDecision decision) {
        requestApprovalRepository.save(
                RequestApproval.builder()
                        .fundRequest(fundRequest)
                        .level(request.getLevel())
                        .decision(decision)
                        .approverEmail(currentUserEmail)
                        .approverName(request.getApproverName())
                        .note(request.getNote())
                        .build());
    }

    private void saveStatusHistory(
            FundRequest fundRequest,
            FundRequestStatus oldStatus,
            FundRequestStatus newStatus,
            String currentUserEmail,
            String note) {
        requestStatusHistoryRepository.save(
                RequestStatusHistory.builder()
                        .fundRequest(fundRequest)
                        .oldStatus(oldStatus)
                        .newStatus(newStatus)
                        .changedByEmail(currentUserEmail)
                        .note(note)
                        .build());
    }
}