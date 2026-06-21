package com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AttendanceRequest(
        @NotNull AttendanceStatus status,
        String note,
        @Pattern(regexp = "^(http|https)://.*", message = "Format URL tidak valid. Harus dimulai dengan http:// atau https://")
        String evidenceUrl
) {}
