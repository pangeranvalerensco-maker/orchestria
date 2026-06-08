package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AssignmentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MemberAssignmentRequest {
    
    @NotNull(message = "ID anggota wajib diisi")
    private Long memberId;

    @NotNull(message = "ID periode wajib diisi")
    private Long periodId;

    @NotNull(message = "ID divisi wajib diisi")
    private Long divisionId;

    @NotNull(message = "ID jabatan wajib diisi")
    private Long positionId;

    private AssignmentStatus status = AssignmentStatus.ACTIVE;
}
