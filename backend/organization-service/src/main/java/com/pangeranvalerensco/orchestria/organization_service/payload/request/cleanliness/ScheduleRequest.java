package com.pangeranvalerensco.orchestria.organization_service.payload.request.cleanliness;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.ScheduleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ScheduleRequest(
        @NotBlank String title,
        @NotNull LocalDate dutyDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank String location,
        String description,
        @NotNull ScheduleStatus status,
        List<Long> memberIds
) {}
