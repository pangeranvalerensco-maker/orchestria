package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import tools.jackson.databind.ObjectMapper;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.*;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.*;
import com.pangeranvalerensco.orchestria.notification_report_service.event.ReportGeneratedEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.exception.UpstreamServiceException;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.NotificationLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ReportExportLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ReportSubscriberRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportSubscriberRepository reportSubscriberRepository;
    private final ReportExportLogRepository reportExportLogRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ScheduledJobLogRepository scheduledJobLogRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.services.request-service.url:http://localhost:8003}")
    private String requestServiceBaseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            
    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Override
    public ByteArrayOutputStream generateFundRequestExcel(String authorizationHeader, String requestedByEmail) {
        log.info("[REPORT] Memulai pembuatan laporan fund requests Excel");

        String filename = "fund-requests_" + LocalDateTime.now().format(FILENAME_FORMATTER) + ".xlsx";

        ReportExportLog exportLog = ReportExportLog.builder()
                .id(java.util.UUID.randomUUID().toString())
                .reportType(ReportType.FUND_REQUEST)
                .filename(filename)
                .status(ReportExportStatus.PROCESSING)
                .createdAt(LocalDateTime.now())
                .requestedByEmail(requestedByEmail)
                .createdByEmail(requestedByEmail)
                .build();
        exportLog = reportExportLogRepository.save(exportLog);

        try {
            List<FundRequestDto> requests = fetchFundRequests(authorizationHeader);

            ByteArrayOutputStream out = buildExcel(requests);

            exportLog.setRecordCount(requests.size());
            exportLog.setStatus(ReportExportStatus.SUCCESS);
            exportLog.setFinishedAt(LocalDateTime.now());
            reportExportLogRepository.save(exportLog);

            log.info("[REPORT] Laporan selesai dibuat dengan {} record", requests.size());
            
            eventPublisher.publishEvent(new ReportGeneratedEvent(
                    this, requestedByEmail, filename, requests.size(), LocalDateTime.now()
            ));
            
            return out;
        } catch (UpstreamServiceException e) {
            exportLog.setStatus(ReportExportStatus.FAILED);
            exportLog.setErrorMessage("502 Bad Gateway: " + e.getMessage());
            exportLog.setFinishedAt(LocalDateTime.now());
            reportExportLogRepository.save(exportLog);
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_GATEWAY, e.getMessage(), e);
        } catch (Exception e) {
            exportLog.setStatus(ReportExportStatus.FAILED);
            exportLog.setErrorMessage(e.getMessage());
            exportLog.setFinishedAt(LocalDateTime.now());
            reportExportLogRepository.save(exportLog);
            throw new RuntimeException("Gagal generate report", e);
        }
    }

    @Override
    public Map<String, Object> getReportSummary(String authorizationHeader) {
        log.info("[REPORT] Mengambil summary laporan fund requests");
        List<FundRequestDto> requests = fetchFundRequests(authorizationHeader);
        
        long totalRequests = requests.size();
        long pendingApprovalCount = requests.stream().filter(r -> "PENDING".equals(r.getStatus())).count();
        long readyForDisbursementCount = requests.stream().filter(r -> "APPROVED".equals(r.getStatus())).count();
        long disbursedCount = requests.stream().filter(r -> "DISBURSED".equals(r.getStatus())).count();
        long settlementPendingCount = requests.stream().filter(r -> "SETTLEMENT_PENDING".equals(r.getStatus())).count();
        long completedCount = requests.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count();
        
        BigDecimal totalRequestedAmount = requests.stream()
            .map(FundRequestDto::getTotalAmount)
            .filter(java.util.Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
            
        Map<String, Long> requestCountByStatus = new HashMap<>();
        requests.forEach(r -> {
            String status = r.getStatus() != null ? r.getStatus() : "UNKNOWN";
            requestCountByStatus.put(status, requestCountByStatus.getOrDefault(status, 0L) + 1);
        });
        
        long notificationPendingCount = notificationLogRepository.findByStatus(NotificationStatus.PENDING).size();
        long notificationSentCount = notificationLogRepository.findByStatus(NotificationStatus.SENT).size();
        long notificationFailedCount = notificationLogRepository.findByStatus(NotificationStatus.FAILED).size();
        
        long schedulerSuccessCount = scheduledJobLogRepository.findByStatus(SchedulerExecutionStatus.SUCCESS).size();
        long schedulerFailedCount = scheduledJobLogRepository.findByStatus(SchedulerExecutionStatus.FAILED).size();
            
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalRequests", totalRequests);
        summary.put("totalRequestedAmount", totalRequestedAmount);
        summary.put("requestCountByStatus", requestCountByStatus);
        summary.put("pendingApprovalCount", pendingApprovalCount);
        summary.put("readyForDisbursementCount", readyForDisbursementCount);
        summary.put("disbursedCount", disbursedCount);
        summary.put("settlementPendingCount", settlementPendingCount);
        summary.put("completedCount", completedCount);
        
        summary.put("notificationPendingCount", notificationPendingCount);
        summary.put("notificationSentCount", notificationSentCount);
        summary.put("notificationFailedCount", notificationFailedCount);
        
        summary.put("schedulerSuccessCount", schedulerSuccessCount);
        summary.put("schedulerFailedCount", schedulerFailedCount);
        
        return summary;
    }

    @Override
    public List<FundRequestDto> fetchFundRequests(String authorizationHeader) {
        String url = requestServiceBaseUrl
                + "/api/requests?size=1000&page=0&sortBy=createdAt&sortDirection=desc";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, authorizationHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!rawResponse.getStatusCode().is2xxSuccessful()) {
                throw new UpstreamServiceException(
                        "Gagal mengambil data laporan dari request-service"
                );
            }

            String responseBody = rawResponse.getBody();
            if (responseBody == null || responseBody.isBlank()) {
                throw new UpstreamServiceException(
                        "Request-service mengembalikan respons kosong saat membuat laporan"
                );
            }

            var typeFactory = objectMapper.getTypeFactory();
            var pageType = typeFactory.constructParametricType(
                    PageResponseDto.class,
                    FundRequestDto.class
            );
            var apiType = typeFactory.constructParametricType(
                    ApiResponseDto.class,
                    pageType
            );

            ApiResponseDto<PageResponseDto<FundRequestDto>> apiResponse =
                    objectMapper.readValue(responseBody, apiType);

            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                throw new UpstreamServiceException(
                        "Request-service mengembalikan respons tidak valid saat membuat laporan"
                );
            }

            List<FundRequestDto> content = apiResponse.getData().getContent();
            if (content == null || content.isEmpty()) {
                throw new UpstreamServiceException("Tidak ada data fund requests");
            }
            return content;

        } catch (UpstreamServiceException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            throw new UpstreamServiceException(
                    "Gagal mengambil data laporan dari request-service",
                    exception
            );
        } catch (RestClientException exception) {
            throw new UpstreamServiceException(
                    "Request-service tidak dapat dihubungi untuk membuat laporan",
                    exception
            );
        } catch (Exception exception) {
            throw new UpstreamServiceException(
                    "Respons request-service tidak dapat diproses untuk membuat laporan",
                    exception
            );
        }
    }

    private ByteArrayOutputStream buildExcel(List<FundRequestDto> requests) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fund Requests");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            String[] headers = {
                    "ID",
                    "Judul",
                    "Divisi",
                    "Pemohon",
                    "Status",
                    "Total (Rp)",
                    "Tanggal Dibuat"
            };

            Row headerRow = sheet.createRow(0);
            for (int index = 0; index < headers.length; index++) {
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(headers[index]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(index, 20 * 256);
            }

            int rowNumber = 1;
            for (FundRequestDto request : requests) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(request.getId() != null ? request.getId() : 0L);
                row.createCell(1).setCellValue(safe(request.getTitle()));
                row.createCell(2).setCellValue(safe(request.getDivisionName()));
                row.createCell(3).setCellValue(safe(request.getRequesterName()));
                row.createCell(4).setCellValue(safe(request.getStatus()));

                Cell totalCell = row.createCell(5);
                totalCell.setCellValue(
                        request.getTotalAmount() != null
                                ? request.getTotalAmount().doubleValue()
                                : 0.0
                );
                totalCell.setCellStyle(currencyStyle);

                row.createCell(6).setCellValue(
                        request.getCreatedAt() != null
                                ? request.getCreatedAt().format(DATE_FORMATTER)
                                : "-"
                );
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return output;
        } catch (IOException exception) {
            throw new IllegalStateException("Gagal membuat laporan Excel", exception);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        return style;
    }

    private String safe(String value) {
        return value != null ? value : "-";
    }

    @Override
    public ByteArrayOutputStream generateSubscriberTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Subscribers Template");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {
                    "Name",
                    "Email",
                    "Report Type",
                    "Active"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 30 * 256);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat template", e);
        }
    }

    @Override
    public ImportSummary importSubscribers(InputStream inputStream) {
        ImportSummary summary = new ImportSummary();
        int totalRows = 0;
        int importedRows = 0;
        int updatedRows = 0;
        int failedRows = 0;

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null || 
                !safeString(headerRow.getCell(0)).equalsIgnoreCase("Name") ||
                !safeString(headerRow.getCell(1)).equalsIgnoreCase("Email") ||
                !safeString(headerRow.getCell(2)).equalsIgnoreCase("Report Type") ||
                !safeString(headerRow.getCell(3)).equalsIgnoreCase("Active")) {
                throw new IllegalArgumentException("Header tidak valid. Harus Name, Email, Report Type, Active");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String name = safeString(row.getCell(0));
                String email = safeString(row.getCell(1));
                String reportTypeStr = safeString(row.getCell(2));
                String activeStr = safeString(row.getCell(3));

                if (email.isBlank() && name.isBlank() && reportTypeStr.isBlank() && activeStr.isBlank()) {
                    continue; 
                }
                
                totalRows++;

                if (email.isBlank() || !email.contains("@")) {
                    summary.getErrors().add(new ImportSummary.ImportError(i + 1, "Email tidak valid"));
                    failedRows++;
                    continue;
                }

                ReportType reportType;
                try {
                    reportType = ReportType.valueOf(reportTypeStr);
                } catch (IllegalArgumentException e) {
                    summary.getErrors().add(new ImportSummary.ImportError(i + 1, "Report Type tidak valid: " + reportTypeStr));
                    failedRows++;
                    continue;
                }

                boolean active = activeStr.equalsIgnoreCase("true") || activeStr.equalsIgnoreCase("yes") || activeStr.equals("1");

                Optional<ReportSubscriber> existingOpt = reportSubscriberRepository.findByEmailAndReportType(email, reportType);
                ReportSubscriber subscriber;
                
                if (existingOpt.isPresent()) {
                    subscriber = existingOpt.get();
                    subscriber.setName(name);
                    subscriber.setActive(active);
                    updatedRows++;
                } else {
                    subscriber = ReportSubscriber.builder()
                            .email(email)
                            .name(name)
                            .reportType(reportType)
                            .active(active)
                            .build();
                    importedRows++;
                }

                try {
                    reportSubscriberRepository.save(subscriber);
                } catch (Exception e) {
                    summary.getErrors().add(new ImportSummary.ImportError(i + 1, "Gagal menyimpan ke database: " + e.getMessage()));
                    if (existingOpt.isPresent()) updatedRows--;
                    else importedRows--;
                    failedRows++;
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gagal membaca file Excel", e);
        }

        summary.setTotalRows(totalRows);
        summary.setImportedRows(importedRows);
        summary.setUpdatedRows(updatedRows);
        summary.setFailedRows(failedRows);
        return summary;
    }

    private String safeString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    @Override
    public List<ReportSubscriber> getSubscribers() {
        return reportSubscriberRepository.findAll();
    }

    @Override
    public Page<ReportExportLog> getExportLogs(Pageable pageable) {
        return reportExportLogRepository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
