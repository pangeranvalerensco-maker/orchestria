package com.pangeranvalerensco.orchestria.organization_service.payload.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizationPeriodResponse {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean currentPeriod;
    private Boolean publicVisible;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
