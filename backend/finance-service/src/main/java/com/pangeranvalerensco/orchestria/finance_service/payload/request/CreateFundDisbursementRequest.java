package com.pangeranvalerensco.orchestria.finance_service.payload.request;

import com.pangeranvalerensco.orchestria.finance_service.entity.enums.DisbursementMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateFundDisbursementRequest {

    @NotNull(message = "fundRequestId wajib diisi")
    private Long fundRequestId;

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