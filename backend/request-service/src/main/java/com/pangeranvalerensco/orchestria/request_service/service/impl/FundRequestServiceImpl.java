package com.pangeranvalerensco.orchestria.request_service.service.impl;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestItem;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateFundRequestRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.PageResponse;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestItemResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestItemRepository;
import com.pangeranvalerensco.orchestria.request_service.service.FundRequestService;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import com.pangeranvalerensco.orchestria.request_service.payload.request.SubmitSettlementRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.RequestSettlementResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestSettlementRepository;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FundRequestServiceImpl implements FundRequestService {

        private final FundRequestRepository fundRequestRepository;
        private final RequestItemRepository requestItemRepository;
        private final RequestStatusHistoryRepository requestStatusHistoryRepository;
        private final RequestSettlementRepository requestSettlementRepository;

        @Override
        @Transactional
        public FundRequestResponse create(CreateFundRequestRequest request, String currentUserEmail) {
                FundRequest fundRequest = FundRequest.builder()
                                .divisionId(request.getDivisionId())
                                .divisionName(request.getDivisionName())
                                .requesterMemberId(request.getRequesterMemberId())
                                .requesterName(request.getRequesterName())
                                .requesterAuthUserId(request.getRequesterAuthUserId())
                                .title(request.getTitle())
                                .description(request.getDescription())
                                .activityDate(request.getActivityDate())
                                .priority(request.getPriority())
                                .status(FundRequestStatus.DRAFT)
                                .createdByEmail(currentUserEmail)
                                .updatedByEmail(currentUserEmail)
                                .build();

                FundRequest saved = fundRequestRepository.save(fundRequest);

                return mapToResponse(saved);
        }

        @Override
        @Transactional(readOnly = true)
        public PageResponse<FundRequestResponse> getAll(
                        FundRequestStatus status,
                        Long divisionId,
                        Long requesterMemberId,
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

                Specification<FundRequest> specification = activeOnly();

                if (status != null) {
                        specification = specification.and(hasStatus(status));
                }

                if (divisionId != null) {
                        specification = specification.and(hasDivisionId(divisionId));
                }

                if (requesterMemberId != null) {
                        specification = specification.and(hasRequesterMemberId(requesterMemberId));
                }

                Page<FundRequest> pageResult = fundRequestRepository.findAll(specification, pageable);

                return PageResponse.<FundRequestResponse>builder()
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
        @Transactional
        public FundRequestResponse submit(Long id, String currentUserEmail) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                validateOwner(fundRequest, currentUserEmail);

                if (fundRequest.getStatus() != FundRequestStatus.DRAFT) {
                        throw new BadRequestException("Hanya pengajuan berstatus DRAFT yang bisa disubmit");
                }

                if (fundRequest.getTotalAmount() == null
                                || fundRequest.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new BadRequestException(
                                        "Pengajuan harus memiliki minimal satu item dengan total lebih dari 0 sebelum disubmit");
                }

                FundRequestStatus oldStatus = fundRequest.getStatus();

                fundRequest.setStatus(FundRequestStatus.SUBMITTED);
                fundRequest.setSubmittedAt(LocalDateTime.now());
                fundRequest.setUpdatedByEmail(currentUserEmail);

                FundRequest saved = fundRequestRepository.save(fundRequest);

                requestStatusHistoryRepository.save(
                                RequestStatusHistory.builder()
                                                .fundRequest(saved)
                                                .oldStatus(oldStatus)
                                                .newStatus(FundRequestStatus.SUBMITTED)
                                                .changedByEmail(currentUserEmail)
                                                .note("Pengajuan dana disubmit")
                                                .build());

                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public FundRequestResponse markDisbursed(Long id, String currentUserEmail) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                if (fundRequest.getStatus() != FundRequestStatus.READY_FOR_DISBURSEMENT) {
                        throw new BadRequestException(
                                        "Pengajuan hanya bisa ditandai cair jika statusnya READY_FOR_DISBURSEMENT");
                }

                FundRequestStatus oldStatus = fundRequest.getStatus();

                fundRequest.setStatus(FundRequestStatus.DISBURSED);
                fundRequest.setUpdatedByEmail(currentUserEmail);

                FundRequest saved = fundRequestRepository.save(fundRequest);

                requestStatusHistoryRepository.save(
                                RequestStatusHistory.builder()
                                                .fundRequest(saved)
                                                .oldStatus(oldStatus)
                                                .newStatus(FundRequestStatus.DISBURSED)
                                                .changedByEmail(currentUserEmail)
                                                .note("Dana pengajuan sudah dicairkan oleh finance-service")
                                                .build());

                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public FundRequestResponse markFundReceived(Long id, String currentUserEmail) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                validateOwner(fundRequest, currentUserEmail);

                if (fundRequest.getStatus() != FundRequestStatus.DISBURSED) {
                        throw new BadRequestException(
                                        "Dana hanya bisa dikonfirmasi diterima jika status pengajuan DISBURSED");
                }

                FundRequestStatus oldStatus = fundRequest.getStatus();

                fundRequest.setStatus(FundRequestStatus.FUND_RECEIVED);
                fundRequest.setUpdatedByEmail(currentUserEmail);

                FundRequest saved = fundRequestRepository.save(fundRequest);

                requestStatusHistoryRepository.save(
                                RequestStatusHistory.builder()
                                                .fundRequest(saved)
                                                .oldStatus(oldStatus)
                                                .newStatus(FundRequestStatus.FUND_RECEIVED)
                                                .changedByEmail(currentUserEmail)
                                                .note("Dana pengajuan sudah dikonfirmasi diterima")
                                                .build());

                return mapToResponse(saved);
        }

        @Override
        @Transactional
        public RequestSettlementResponse submitSettlement(
                        Long id,
                        SubmitSettlementRequest request,
                        String currentUserEmail) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                validateOwner(fundRequest, currentUserEmail);

                if (fundRequest.getStatus() != FundRequestStatus.FUND_RECEIVED) {
                        throw new BadRequestException(
                                        "Settlement hanya bisa dikirim setelah dana dikonfirmasi diterima");
                }

                if (requestSettlementRepository.existsByFundRequestAndActiveTrue(fundRequest)) {
                        throw new BadRequestException("Settlement untuk pengajuan ini sudah pernah dikirim");
                }

                BigDecimal requestedAmount = fundRequest.getTotalAmount();
                BigDecimal spentAmount = request.getSpentAmount();

                BigDecimal remainingAmount = BigDecimal.ZERO;
                BigDecimal shortageAmount = BigDecimal.ZERO;

                if (spentAmount.compareTo(requestedAmount) < 0) {
                        remainingAmount = requestedAmount.subtract(spentAmount);
                } else if (spentAmount.compareTo(requestedAmount) > 0) {
                        shortageAmount = spentAmount.subtract(requestedAmount);
                }

                RequestSettlement settlement = RequestSettlement.builder()
                                .fundRequest(fundRequest)
                                .spentAmount(spentAmount)
                                .remainingAmount(remainingAmount)
                                .shortageAmount(shortageAmount)
                                .proofUrl(request.getProofUrl())
                                .note(request.getNote())
                                .submittedByEmail(currentUserEmail)
                                .active(true)
                                .build();

                RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);

                FundRequestStatus oldStatus = fundRequest.getStatus();

                fundRequest.setStatus(FundRequestStatus.SETTLEMENT_SUBMITTED);
                fundRequest.setUpdatedByEmail(currentUserEmail);

                FundRequest savedFundRequest = fundRequestRepository.save(fundRequest);

                requestStatusHistoryRepository.save(
                                RequestStatusHistory.builder()
                                                .fundRequest(savedFundRequest)
                                                .oldStatus(oldStatus)
                                                .newStatus(FundRequestStatus.SETTLEMENT_SUBMITTED)
                                                .changedByEmail(currentUserEmail)
                                                .note("Settlement penggunaan dana dikirim")
                                                .build());

                return mapSettlementToResponse(savedSettlement);
        }

        @Override
        @Transactional
        public RequestSettlementResponse approveSettlement(Long id, String currentUserEmail) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                if (fundRequest.getStatus() != FundRequestStatus.SETTLEMENT_SUBMITTED) {
                        throw new BadRequestException(
                                        "Settlement hanya bisa disetujui jika status pengajuan SETTLEMENT_SUBMITTED");
                }

                RequestSettlement settlement = requestSettlementRepository.findByFundRequestAndActiveTrue(fundRequest)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Settlement pengajuan tidak ditemukan"));

                settlement.setApprovedByEmail(currentUserEmail);
                settlement.setApprovedAt(LocalDateTime.now());

                RequestSettlement savedSettlement = requestSettlementRepository.save(settlement);

                FundRequestStatus oldStatus = fundRequest.getStatus();

                fundRequest.setStatus(FundRequestStatus.COMPLETED);
                fundRequest.setCompletedAt(LocalDateTime.now());
                fundRequest.setUpdatedByEmail(currentUserEmail);

                FundRequest savedFundRequest = fundRequestRepository.save(fundRequest);

                requestStatusHistoryRepository.save(
                                RequestStatusHistory.builder()
                                                .fundRequest(savedFundRequest)
                                                .oldStatus(oldStatus)
                                                .newStatus(FundRequestStatus.COMPLETED)
                                                .changedByEmail(currentUserEmail)
                                                .note("Settlement disetujui dan pengajuan selesai")
                                                .build());

                return mapSettlementToResponse(savedSettlement);
        }

        @Override
        @Transactional(readOnly = true)
        public FundRequestResponse getById(Long id) {
                FundRequest fundRequest = fundRequestRepository.findById(id)
                                .filter(FundRequest::getActive)
                                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

                return mapToResponse(fundRequest);
        }

        private Specification<FundRequest> activeOnly() {
                return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("active"));
        }

        private Specification<FundRequest> hasStatus(FundRequestStatus status) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status);
        }

        private Specification<FundRequest> hasDivisionId(Long divisionId) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("divisionId"), divisionId);
        }

        private Specification<FundRequest> hasRequesterMemberId(Long requesterMemberId) {
                return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("requesterMemberId"),
                                requesterMemberId);
        }

        private FundRequestResponse mapToResponse(FundRequest fundRequest) {
                List<RequestItem> items = requestItemRepository
                                .findByFundRequestAndActiveTrueOrderByCreatedAtAsc(fundRequest);

                List<RequestItemResponse> itemResponses = items.stream()
                                .map(item -> RequestItemResponse.builder()
                                                .id(item.getId())
                                                .itemName(item.getItemName())
                                                .description(item.getDescription())
                                                .quantity(item.getQuantity())
                                                .unitPrice(item.getUnitPrice())
                                                .subtotal(item.getSubtotal())
                                                .active(item.getActive())
                                                .createdAt(item.getCreatedAt())
                                                .updatedAt(item.getUpdatedAt())
                                                .build())
                                .toList();

                return FundRequestResponse.builder()
                                .id(fundRequest.getId())
                                .divisionId(fundRequest.getDivisionId())
                                .divisionName(fundRequest.getDivisionName())
                                .requesterMemberId(fundRequest.getRequesterMemberId())
                                .requesterName(fundRequest.getRequesterName())
                                .requesterAuthUserId(fundRequest.getRequesterAuthUserId())
                                .title(fundRequest.getTitle())
                                .description(fundRequest.getDescription())
                                .activityDate(fundRequest.getActivityDate())
                                .priority(fundRequest.getPriority())
                                .status(fundRequest.getStatus())
                                .totalAmount(fundRequest.getTotalAmount())
                                .submittedAt(fundRequest.getSubmittedAt())
                                .completedAt(fundRequest.getCompletedAt())
                                .active(fundRequest.getActive())
                                .createdByEmail(fundRequest.getCreatedByEmail())
                                .updatedByEmail(fundRequest.getUpdatedByEmail())
                                .createdAt(fundRequest.getCreatedAt())
                                .updatedAt(fundRequest.getUpdatedAt())
                                .items(itemResponses)
                                .build();
        }

        private RequestSettlementResponse mapSettlementToResponse(RequestSettlement settlement) {
                FundRequest fundRequest = settlement.getFundRequest();

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

        @Override
        @Transactional(readOnly = true)
        public PageResponse<FundRequestResponse> getMyRequests(
                        String currentUserEmail,
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

                Specification<FundRequest> specification = (root, query, criteriaBuilder) -> criteriaBuilder.and(
                                criteriaBuilder.isTrue(root.get("active")),
                                criteriaBuilder.equal(root.get("createdByEmail"), currentUserEmail));

                Page<FundRequest> pageResult = fundRequestRepository.findAll(specification, pageable);

                return PageResponse.<FundRequestResponse>builder()
                                .content(pageResult.getContent()
                                                .stream()
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

        private void validateOwner(FundRequest fundRequest, String currentUserEmail) {
                String ownerEmail = fundRequest.getCreatedByEmail();

                if (ownerEmail == null
                                || currentUserEmail == null
                                || !ownerEmail.equalsIgnoreCase(currentUserEmail)) {
                        throw new ForbiddenException(
                                        "Anda tidak memiliki akses untuk memproses pengajuan ini");
                }
        }
}