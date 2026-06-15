package com.pangeranvalerensco.orchestria.request_service.payload.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RequestItemCreateRequest {

    @NotBlank(message = "Nama item wajib diisi")
    private String itemName;

    private String description;

    @Min(value = 1, message = "Quantity minimal 1")
    private Integer quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "Harga satuan tidak boleh negatif")
    private BigDecimal unitPrice;
}