package com.pangeranvalerensco.orchestria.request_service.payload.request;

import com.pangeranvalerensco.orchestria.request_service.entity.enums.RequestPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateFundRequestRequest {
    
    @NotNull(message = "divisionId Wajib diisi")
    private Long divisionId;

    @NotBlank(message = "divisionName wajib diisi")
    @Size(max = 150, message = "divisionName maksimal 150 karakter")
    private String divisionName;

    @NotNull(message = "requestMemberId wajib diisi")
    private Long requesterMemberId;

    @NotBlank(message = "title wajib diisi")
    @Size(max = 150, message = "title maksimal 150 karakter")
    private String title;

    private String description;

    private LocalDate activityDate;

    private RequestPriority priority;
}
