package com.pangeranvalerensco.orchestria.organization_service.payload.request;

import java.time.LocalDate;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskPriority;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DivisionTaskRequest {
    
    @NotNull(message = "ID divisi wajib diisi")
    private Long divisionId;

    private Long assignedMemberId;

    @NotBlank(message = "Judul tugas wajib diisi")
    @Size(max = 150, message = "Judul Tugas maksimal 150 karakter")
    private String title;

    @Size(max = 1000, message = "Deskripsi maksimal 1000 karakter")
    private String description;

    private LocalDate dueDate;

    private TaskStatus status = TaskStatus.TODO;

    private TaskPriority priority = TaskPriority.MEDIUM;
}
