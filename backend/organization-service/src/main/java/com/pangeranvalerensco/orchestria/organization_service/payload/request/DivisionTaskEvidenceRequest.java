package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DivisionTaskEvidenceRequest {

    @NotNull(message = "ID tugas divisi wajib diisi")
    private Long taskId;

    private EvidenceType type = EvidenceType.NOTE;

    @NotBlank(message = "Judul bukti wajib diisi")
    @Size(max = 150, message = "Judul bukti maksimal 150 karakter")
    private String title;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    @Size(max = 500, message = "URL file maksimal 500 karakter")
    private String fileUrl;

    @Size(max = 500, message = "Link eksternal maksimal 500 karakter")
    private String externalLink;
}