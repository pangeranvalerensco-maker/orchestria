package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import com.pangeranvalerensco.orchestria.organization_service.entity.PublicContentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicContentRequest {
    @NotNull(message = "Tipe konten tidak boleh kosong")
    private PublicContentType contentType;

    private String title;
    private String subtitle;
    private String body;
    private String category;
    private String statusLabel;
    private LocalDate eventDate;
    private String mediaUrl;
    private String linkUrl;
    private String authorName;
    private String authorRole;

    @NotNull(message = "Urutan tampilan tidak boleh kosong")
    @Min(value = 0, message = "Urutan tampilan tidak boleh negatif")
    private Integer displayOrder;
}
