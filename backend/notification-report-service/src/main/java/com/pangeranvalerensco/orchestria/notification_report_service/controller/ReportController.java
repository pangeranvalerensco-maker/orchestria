package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller untuk endpoint pembuatan laporan.
 *
 * Endpoint:
 *   GET /api/reports/fund-requests.xlsx
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Generate dan download laporan fund request dalam format Excel (.xlsx).
     *
     * Service mengambil data dari request-service, membangun file Excel
     * dengan Apache POI, lalu mengembalikan sebagai file download.
     *
     * @return file .xlsx sebagai byte stream
     */
    @GetMapping("/fund-requests.xlsx")
    public ResponseEntity<byte[]> downloadFundRequestReport() {
        log.info("[REPORT-CONTROLLER] Permintaan unduh laporan fund-requests.xlsx");

        ByteArrayOutputStream excelData = reportService.generateFundRequestExcel();

        String filename = "fund-requests_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData.toByteArray());
    }
}
