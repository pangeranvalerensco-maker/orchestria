package com.pangeranvalerensco.orchestria.request_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestStatusHistory;

public interface RequestStatusHistoryRepository extends JpaRepository<RequestStatusHistory, Long> {
 
    List<RequestStatusHistory> findByFundRequestOrderByChangedAtAsc(FundRequest fundRequest);
}
