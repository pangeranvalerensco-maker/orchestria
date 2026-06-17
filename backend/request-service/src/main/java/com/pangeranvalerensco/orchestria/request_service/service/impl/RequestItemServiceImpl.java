package com.pangeranvalerensco.orchestria.request_service.service.impl;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestItem;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import com.pangeranvalerensco.orchestria.request_service.exception.BadRequestException;
import com.pangeranvalerensco.orchestria.request_service.exception.ResourceNotFoundException;
import com.pangeranvalerensco.orchestria.request_service.exception.ForbiddenException;
import com.pangeranvalerensco.orchestria.request_service.payload.request.CreateRequestItemRequest;
import com.pangeranvalerensco.orchestria.request_service.payload.response.FundRequestResponse;
import com.pangeranvalerensco.orchestria.request_service.repository.FundRequestRepository;
import com.pangeranvalerensco.orchestria.request_service.repository.RequestItemRepository;
import com.pangeranvalerensco.orchestria.request_service.service.RequestItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RequestItemServiceImpl implements RequestItemService {

    private final FundRequestRepository fundRequestRepository;
    private final RequestItemRepository requestItemRepository;
    private final FundRequestServiceImpl fundRequestService;

    @Override
    @Transactional
    public FundRequestResponse addItem(
            Long fundRequestId,
            CreateRequestItemRequest request,
            String currentUserEmail) {
        FundRequest fundRequest = fundRequestRepository.findById(fundRequestId)
                .filter(FundRequest::getActive)
                .orElseThrow(() -> new ResourceNotFoundException("Pengajuan dana tidak ditemukan"));

        if (fundRequest.getCreatedByEmail() == null
                || !fundRequest.getCreatedByEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ForbiddenException(
                    "Anda tidak memiliki akses untuk mengubah item pengajuan ini");
        }

        if (fundRequest.getStatus() != FundRequestStatus.DRAFT) {
            throw new BadRequestException("Item hanya bisa ditambahkan ketika pengajuan masih berstatus DRAFT");
        }

        RequestItem item = RequestItem.builder()
                .fundRequest(fundRequest)
                .itemName(request.getItemName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .active(true)
                .build();

        requestItemRepository.save(item);

        BigDecimal totalAmount = requestItemRepository.sumActiveSubtotalByFundRequest(fundRequest);

        fundRequest.setTotalAmount(totalAmount);
        fundRequest.setUpdatedByEmail(currentUserEmail);

        FundRequest savedFundRequest = fundRequestRepository.save(fundRequest);

        return fundRequestService.getById(savedFundRequest.getId());
    }
}