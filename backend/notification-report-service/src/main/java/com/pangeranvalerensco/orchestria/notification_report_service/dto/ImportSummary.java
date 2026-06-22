package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSummary {
    private int totalRows;
    private int importedRows;
    private int updatedRows;
    private int failedRows;
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ImportError {
        private int rowNumber;
        private String message;
    }
}
