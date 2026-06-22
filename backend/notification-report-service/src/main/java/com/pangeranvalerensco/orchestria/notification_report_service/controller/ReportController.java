package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final ReportService reportService;

    @PreAuthorize("hasAuthority('report.export')")
    @GetMapping("/fund-requests.xlsx")
    public ResponseEntity<byte[]> downloadFundRequestReport(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        log.info("[REPORT-CONTROLLER] Permintaan unduh laporan fund-requests.xlsx");

        ByteArrayOutputStream excelData =
                reportService.generateFundRequestExcel(authorizationHeader);
        String filename = "fund-requests_"
                + LocalDateTime.now().format(FILENAME_FORMATTER)
                + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData.toByteArray());
    }

    @PreAuthorize("hasAuthority('report.read')")
    @GetMapping("/summary")
    public ResponseEntity<java.util.Map<String, Object>> getReportSummary(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        log.info("[REPORT-CONTROLLER] Permintaan summary laporan");
        return ResponseEntity.ok(reportService.getReportSummary(authorizationHeader));
    }

    @PreAuthorize("hasAuthority('report.import')")
    @GetMapping("/subscribers/template.xlsx")
    public ResponseEntity<byte[]> downloadSubscriberTemplate() {
        log.info("[REPORT-CONTROLLER] Permintaan unduh template subscribers");

        ByteArrayOutputStream excelData = reportService.generateSubscriberTemplate();
        String filename = "subscribers-template.xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelData.toByteArray());
    }

    @PreAuthorize("hasAuthority('report.import')")
    @PostMapping("/subscribers/import")
    public ResponseEntity<Void> importSubscribers(@RequestParam("file") MultipartFile file) {
        log.info("[REPORT-CONTROLLER] Menerima import file subscribers: {}", file.getOriginalFilename());
        try {
            reportService.importSubscribers(file.getInputStream());
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            log.error("Gagal membaca file import", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PreAuthorize("hasAuthority('report.read')")
    @GetMapping("/subscribers")
    public ResponseEntity<List<com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportSubscriber>> getSubscribers() {
        return ResponseEntity.ok(reportService.getSubscribers());
    }

    @PreAuthorize("hasAuthority('report.read')")
    @GetMapping("/exports")
    public ResponseEntity<Page<com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportExportLog>> getExportLogs(Pageable pageable) {
        return ResponseEntity.ok(reportService.getExportLogs(pageable));
    }
}
