package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrganizationPeriodRequest {
    
    @NotBlank(message = "Nama Periode Wajib Diisi")
    @Size(max = 100, message = "Nama Periode maksimal 100 karakter")
    private String name;

    private LocalDate startDate;

    private LocalDate endDate;

    @NotNull(message = "Status periode aktif wajib diisi")
    private Boolean currentPeriod;

    @NotNull(message = "Status tampil Publik wajib diisi")
    private Boolean publicVisible;
}
