package com.pangeranvalerensco.orchestria.request_service.repository;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestApprovalRepository extends JpaRepository<RequestApproval, Long> {

    List<RequestApproval> findByFundRequestOrderByDecidedAtAsc(FundRequest fundRequest);
}