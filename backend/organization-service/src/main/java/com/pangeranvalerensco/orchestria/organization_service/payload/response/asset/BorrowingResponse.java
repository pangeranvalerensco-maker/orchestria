package com.pangeranvalerensco.orchestria.organization_service.payload.response.asset;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssetCondition;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.BorrowingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BorrowingResponse(
        String id,
        AssetResponse asset,
        String borrowerMemberId,
        String borrowerName,
        String purpose,
        LocalDate borrowDate,
        LocalDate expectedReturnDate,
        LocalDate actualReturnDate,
        BorrowingStatus status,
        boolean overdue,
        String rejectionReason,
        String cancellationReason,
        String approvedByEmail,
        LocalDateTime approvedAt,
        String handedOverByEmail,
        LocalDateTime handedOverAt,
        LocalDateTime returnRequestedAt,
        String returnVerifiedByEmail,
        LocalDateTime returnVerifiedAt,
        AssetCondition conditionBefore,
        AssetCondition conditionAfter,
        String handoverProofUrl,
        String returnProofUrl,
        String note,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
