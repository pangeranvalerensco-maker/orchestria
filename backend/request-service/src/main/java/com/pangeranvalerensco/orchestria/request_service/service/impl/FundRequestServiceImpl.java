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
import com.pangeranvalerensco.orchestria.request_service.repository.RequestStatusHistoryRepository;
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

    @Override
    @Transactional
    public FundRequestResponse submit(Long id, String currentUserEmail) {
        FundRequest fundRequest = fundRequestRepository.findById(id)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

        if (fundRequest.getStatus() != FundRequestStatus.DRAFT) {
            throw new BadRequestException("Hanya pengajuan berstatus DRAFT yang bisa disubmit");
        }

        if (fundRequest.getTotalAmount() == null || fundRequest.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
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
}