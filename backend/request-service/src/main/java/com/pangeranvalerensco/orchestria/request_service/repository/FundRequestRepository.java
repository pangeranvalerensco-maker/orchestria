package com.pangeranvalerensco.orchestria.request_service.repository;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.enums.FundRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundRequestRepository extends JpaRepository<FundRequest, Long> {

    Page<FundRequest> findByActiveTrue(Pageable pageable);

    Page<FundRequest> findByActiveTrueAndStatus(FundRequestStatus status, Pageable pageable);

    Page<FundRequest> findByActiveTrueAndDivisionId(Long divisionId, Pageable pageable);

    Page<FundRequest> findByActiveTrueAndRequesterMemberId(Long requesterMemberId, Pageable pageable);

    Page<FundRequest> findByActiveTrueAndStatusAndDivisionId(
            FundRequestStatus status,
            Long divisionId,
            Pageable pageable
    );
}