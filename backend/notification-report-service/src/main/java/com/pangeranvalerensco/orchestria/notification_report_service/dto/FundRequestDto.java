package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO yang mencerminkan FundRequestResponse dari request-service.
 * Digunakan untuk deserialisasi hasil REST call ke request-service.
 *
 * Field dibatasi pada yang diperlukan untuk laporan Excel.
 */
@Data
@NoArgsConstructor
public class FundRequestDto {

    private Long id;
    private String title;
    private String divisionName;
    private String requesterName;
    private String status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private LocalDate activityDate;
    private String description;
    private String priority;
}
