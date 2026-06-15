package com.pangeranvalerensco.orchestria.request_service.payload.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RequestItemResponse {

    private Long id;
    private String itemName;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}