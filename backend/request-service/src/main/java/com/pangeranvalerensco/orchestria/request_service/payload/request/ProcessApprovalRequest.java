package com.pangeranvalerensco.orchestria.request_service.payload.request;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessApprovalRequest {

    @NotNull(message = "level approval wajib diisi")
    private ApprovalLevel level;

    private String note;
}