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

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FundDisbursementServiceImpl implements FundDisbursementService {

        private final FundDisbursementRepository fundDisbursementRepository;
        private final RequestClient requestClient;

        @Override
        @Transactional
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
                                .receiverName(request.getReceiverName())
                                .receiverNote(request.getReceiverNote())
                                .proofUrl(request.getProofUrl())
                                .note(request.getNote())
                                .disbursedByEmail(currentUserEmail)
                                .active(true)
                                .build();

                try {
                        FundDisbursement saved = fundDisbursementRepository.saveAndFlush(
                                        disbursement);

                        return mapToResponse(saved);

                } catch (DataIntegrityViolationException ex) {
                        throw new BadRequestException(
                                        "Pengajuan dana ini sudah pernah dicairkan");
                }
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