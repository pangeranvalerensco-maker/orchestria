package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskPriority;
import com.pangeranvalerensco.orchestria.organization_service.entity.enums.TaskStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DivisionTaskResponse {

    private Long id;

    private Long divisionId;
    private String divisionCode;
    private String divisionName;

    private Long assignedMemberId;
    private String assignedMemberName;
    private String assignedMemberEmail;

    private String title;
    private String description;
    private LocalDate dueDate;

    private TaskStatus status;
    private TaskPriority priority;

    private Boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
