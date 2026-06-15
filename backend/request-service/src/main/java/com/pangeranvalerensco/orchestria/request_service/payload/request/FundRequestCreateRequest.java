package com.pangeranvalerensco.orchestria.request_service.payload.request;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class FundRequestCreateRequest {

    @NotNull(message = "ID divisi wajib diisi")
    private Long divisionId;

    @NotBlank(message = "Nama divisi wajib diisi")
    private String divisionName;

    @NotNull(message = "ID member pengaju wajib diisi")
    private Long requesterMemberId;

    @NotBlank(message = "Nama pengaju wajib diisi")
    private String requesterName;

    private Long requesterAuthUserId;

    @NotBlank(message = "Judul pengajuan wajib diisi")
    private String title;

    private String description;

    private LocalDate activityDate;

    private RequestPriority priority;

    @Valid
    private List<RequestItemCreateRequest> items;
}