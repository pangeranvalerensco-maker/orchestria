package com.pangeranvalerensco.orchestria.request_service.repository;

import com.pangeranvalerensco.orchestria.request_service.entity.FundRequest;
import com.pangeranvalerensco.orchestria.request_service.entity.RequestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface RequestItemRepository extends JpaRepository<RequestItem, Long> {

    List<RequestItem> findByFundRequestAndActiveTrueOrderByCreatedAtAsc(FundRequest fundRequest);

    @Query("""
            SELECT COALESCE(SUM(item.subtotal), 0)
            FROM RequestItem item
            WHERE item.fundRequest = :fundRequest
            AND item.active = true
            """)
    BigDecimal sumActiveSubtotalByFundRequest(FundRequest fundRequest);
}