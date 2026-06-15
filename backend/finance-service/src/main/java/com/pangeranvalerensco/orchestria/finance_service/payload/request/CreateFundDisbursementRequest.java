package com.pangeranvalerensco.orchestria.finance_service.payload.request;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateFundDisbursementRequest {

    @NotNull(message = "fundRequestId wajib diisi")
    private Long fundRequestId;

    @NotBlank(message = "requestTitle wajib diisi")
    @Size(max = 150, message = "requestTitle maksimal 150 karakter")
    private String requestTitle;

    @NotNull(message = "divisionId wajib diisi")
    private Long divisionId;

    @NotBlank(message = "divisionName wajib diisi")
    @Size(max = 150, message = "divisionName maksimal 150 karakter")
    private String divisionName;

    @NotBlank(message = "requesterName wajib diisi")
    @Size(max = 150, message = "requesterName maksimal 150 karakter")
    private String requesterName;

    @NotNull(message = "amount wajib diisi")
    @DecimalMin(value = "1.0", message = "amount minimal 1")
    private BigDecimal amount;

    @NotNull(message = "method wajib diisi")
    private DisbursementMethod method;

    @NotBlank(message = "receiverName wajib diisi")
    @Size(max = 150, message = "receiverName maksimal 150 karakter")
    private String receiverName;

    private String receiverNote;

    @Size(max = 500, message = "proofUrl maksimal 500 karakter")
    private String proofUrl;

    private String note;
}