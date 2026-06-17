package com.pangeranvalerensco.orchestria.finance_service.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FundRequestSnapshotResponse {

    private Long id;

    private Long divisionId;

    private String divisionName;

    private Long requesterMemberId;

    private String requesterName;

    private String title;

    private String status;

    private BigDecimal totalAmount;

    private Boolean active;
}