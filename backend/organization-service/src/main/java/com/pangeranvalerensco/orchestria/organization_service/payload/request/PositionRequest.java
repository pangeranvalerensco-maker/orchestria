package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PositionRequest {
    
    @NotBlank(message = "Kode Jabatan Wajib Diisi")
    @Size(max = 50, message = "Kode Jabatan maksimal 50 karakter")
    private String code;

    @NotBlank(message = "Nama Jabatan Wajib Diisi")
    @Size(max = 100, message = "Nama Jabatan maksimal 100 karakter")
    private String name;

    @Size(max = 500, message = "Deskripsi Jabatan maksimal 500 karakter")
    private String description;

    @NotNull(message = "Level Jabatan Wajib Diisi")
    private Integer levelOrder;

    @NotNull(message = "Status tampil Publik Wajib Diisi")
    private Boolean publicVisible;
}
