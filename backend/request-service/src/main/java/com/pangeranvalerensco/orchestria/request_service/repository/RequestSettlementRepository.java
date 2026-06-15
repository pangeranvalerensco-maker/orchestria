package com.pangeranvalerensco.orchestria.request_service.repository;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestSettlementRepository extends JpaRepository<RequestSettlement, Long> {

    Optional<RequestSettlement> findByFundRequestAndActiveTrue(FundRequest fundRequest);

    boolean existsByFundRequestAndActiveTrue(FundRequest fundRequest);
}