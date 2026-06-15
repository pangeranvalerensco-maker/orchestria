package com.pangeranvalerensco.orchestria.request_service.payload.request;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.ApprovalLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProcessApprovalRequest {

    @NotNull(message = "level approval wajib diisi")
    private ApprovalLevel level;

    @Size(max = 150, message = "approverName maksimal 150 karakter")
    private String approverName;

    private String note;
}