package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.ApiResponseDto;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.PageResponseDto;
import com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementasi ReportService.
 *
 * Alur kerja:
 * 1. Ambil data dari request-service via RestTemplate (HTTP GET)
 * 2. Buat workbook Excel dengan Apache POI
 * 3. Tulis header dan baris data
 * 4. Publikasikan NotificationEvent "REPORT_READY"
 * 5. Return byte array file .xlsx
 *
 * URL request-service dikonfigurasi melalui property:
 *   services.request.base-url
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${services.request.base-url:http://localhost:8003}")
    private String requestServiceBaseUrl;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // =========================================================================
    //  Public API
    // =========================================================================

    @Override
    public ByteArrayOutputStream generateFundRequestExcel() {
        log.info("[REPORT] Memulai pembuatan laporan fund-requests.xlsx...");

        List<FundRequestDto> requests = fetchFundRequests();
        log.info("[REPORT] Data diambil: {} record", requests.size());

        ByteArrayOutputStream out = buildExcel(requests);

        // Publikasikan event bahwa laporan siap — event diproses oleh NotificationEventListener
        eventPublisher.publishEvent(new NotificationEvent(this, "REPORT_READY",
                "Laporan fund-requests.xlsx berhasil dibuat dengan " + requests.size() + " record."));

        log.info("[REPORT] ✓ Laporan selesai dibuat.");
        return out;
    }

    @Override
    public List<FundRequestDto> fetchFundRequests() {
        String url = requestServiceBaseUrl + "/api/requests?size=1000&page=0&sortBy=createdAt&sortDirection=desc";
        log.info("[REPORT] Mengambil data fund request dari: {}", url);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    url, HttpMethod.GET, null, String.class);

            if (rawResponse.getBody() == null) {
                log.warn("[REPORT] Response body null dari request-service.");
                return List.of();
            }

            // Deserialisasi menggunakan TypeFactory Jackson 3.x
            var tf = objectMapper.getTypeFactory();
            var pageType = tf.constructParametricType(PageResponseDto.class, FundRequestDto.class);
            var apiType = tf.constructParametricType(ApiResponseDto.class, pageType);

            ApiResponseDto<PageResponseDto<FundRequestDto>> apiResponse =
                    objectMapper.readValue(rawResponse.getBody(), apiType);

            if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
                log.warn("[REPORT] Response tidak sukses atau data null.");
                return List.of();
            }

            List<FundRequestDto> content = apiResponse.getData().getContent();
            log.info("[REPORT] ✓ Berhasil mengambil {} fund request.", content.size());
            return content != null ? content : List.of();

        } catch (RestClientException e) {
            log.error("[REPORT] ✗ Gagal menghubungi request-service di {}: {}", url, e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("[REPORT] ✗ Error tidak terduga saat fetch data: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // =========================================================================
    //  Excel Builder
    // =========================================================================

    private ByteArrayOutputStream buildExcel(List<FundRequestDto> requests) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fund Requests");

            // ---- Styles ----
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            // ---- Header Row ----
            String[] headers = {"ID", "Judul", "Divisi", "Pemohon", "Status", "Total (Rp)", "Tanggal Dibuat"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            sheet.setColumnWidth(1, 40 * 256);
            sheet.setColumnWidth(2, 25 * 256);
            sheet.setColumnWidth(3, 25 * 256);

            // ---- Data Rows ----
            int rowNum = 1;
            for (FundRequestDto req : requests) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(req.getId() != null ? req.getId() : 0L);
                row.createCell(1).setCellValue(safe(req.getTitle()));
                row.createCell(2).setCellValue(safe(req.getDivisionName()));
                row.createCell(3).setCellValue(safe(req.getRequesterName()));
                row.createCell(4).setCellValue(safe(req.getStatus()));

                Cell totalCell = row.createCell(5);
                totalCell.setCellValue(req.getTotalAmount() != null
                        ? req.getTotalAmount().doubleValue() : 0.0);
                totalCell.setCellStyle(currencyStyle);

                row.createCell(6).setCellValue(req.getCreatedAt() != null
                        ? req.getCreatedAt().format(DATE_FORMATTER) : "-");
            }

            // ---- Auto Filter ----
            sheet.setAutoFilter(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.length - 1));
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out;

        } catch (IOException e) {
            log.error("[REPORT] ✗ Gagal menulis file Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Gagal membuat laporan Excel", e);
        }
    }

    // =========================================================================
    //  Style Helpers
    // =========================================================================

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
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
}
