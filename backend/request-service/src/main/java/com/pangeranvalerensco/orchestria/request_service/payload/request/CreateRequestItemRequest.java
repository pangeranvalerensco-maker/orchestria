package com.pangeranvalerensco.orchestria.request_service.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateRequestItemRequest {

    @NotBlank(message = "itemName wajib diisi")
    private String itemName;

    private String description;

    @Min(value = 1, message = "quantity minimal 1")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice tidak boleh negatif")
    private BigDecimal unitPrice;
}