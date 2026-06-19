package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wrapper paginasi yang mencerminkan PageResponse dari request-service.
 * Digunakan untuk deserialisasi hasil REST call ke request-service.
 */
@Data
@NoArgsConstructor
public class PageResponseDto<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
