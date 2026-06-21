package com.pangeranvalerensco.orchestria.organization_service.payload.request.english;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.EnglishActivityStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EnglishActivityRequest(
        @NotBlank(message = "Title wajib diisi")
        String title,

        @NotNull(message = "Tanggal wajib diisi")
        LocalDate activityDate,

        @NotNull(message = "Waktu mulai wajib diisi")
        LocalTime startTime,

        @NotNull(message = "Waktu selesai wajib diisi")
        LocalTime endTime,

        @NotBlank(message = "Topik wajib diisi")
        String topic,

        String description,

        @NotNull(message = "Status wajib diisi")
        EnglishActivityStatus status
) {
}
