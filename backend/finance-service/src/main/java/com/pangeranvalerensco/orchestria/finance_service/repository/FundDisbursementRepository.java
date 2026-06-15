package com.pangeranvalerensco.orchestria.finance_service.repository;

import com.pangeranvalerensco.orchestria.finance_service.entity.FundDisbursement;
import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FundDisbursementRepository extends JpaRepository<FundDisbursement, Long> {

    Optional<FundDisbursement> findByFundRequestIdAndActiveTrue(Long fundRequestId);

    Page<FundDisbursement> findByActiveTrue(Pageable pageable);

    Page<FundDisbursement> findByActiveTrueAndStatus(DisbursementStatus status, Pageable pageable);

    boolean existsByFundRequestIdAndActiveTrue(Long fundRequestId);
}