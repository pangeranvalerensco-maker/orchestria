package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DivisionRequest {
    
    @NotBlank(message = "Kode Divisi Wajib Diisi")
    @Size(max = 50, message = "Kode Divisi maksimal 50 karakter")
    private String code;

    @NotBlank(message = "Nama Divisi Wajib Diisi")
    @Size(max = 100, message = "Nama Divisi maksimal 100 karakter")
    private String name;

    @Size(max = 500, message = "Deskripsi Divisi maksimal 500 karakter")
    private String description;

    @NotNull(message = "Urutan Tampilan Divisi Wajib Diisi")
    private Integer displayOrder;

    @NotNull(message = "Visibilitas Publik Divisi Wajib Diisi")
    private Boolean publicVisible;
}
