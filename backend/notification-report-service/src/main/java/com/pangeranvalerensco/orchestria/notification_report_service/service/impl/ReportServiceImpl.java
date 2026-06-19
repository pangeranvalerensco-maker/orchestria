package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.ApiResponseDto;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.PageResponseDto;
import com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.exception.UpstreamServiceException;
import com.pangeranvalerensco.orchestria.notification_report_service.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RestTemplate restTemplate;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Value("${services.request.base-url:http://localhost:8003}")
    private String requestServiceBaseUrl;

    @Override
    public ByteArrayOutputStream generateFundRequestExcel(String authorizationHeader) {
        log.info("[REPORT] Memulai pembuatan laporan fund-requests.xlsx");

        List<FundRequestDto> requests = fetchFundRequests(authorizationHeader);
        ByteArrayOutputStream out = buildExcel(requests);

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                "REPORT_READY",
                "Laporan fund-requests.xlsx berhasil dibuat dengan "
                        + requests.size()
                        + " record."
        ));

        log.info("[REPORT] Laporan selesai dibuat dengan {} record", requests.size());
        return out;
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
                log.error(
                        "[REPORT] Request-service merespons HTTP {}",
                        rawResponse.getStatusCode().value()
                );
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
            List<FundRequestDto> result = content != null ? content : List.of();
            log.info("[REPORT] Berhasil mengambil {} fund request", result.size());
            return result;

        } catch (UpstreamServiceException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.error(
                    "[REPORT] Request-service merespons HTTP {}",
                    exception.getStatusCode().value()
            );
            throw new UpstreamServiceException(
                    "Gagal mengambil data laporan dari request-service",
                    exception
            );
        } catch (RestClientException exception) {
            log.error("[REPORT] Request-service tidak dapat dihubungi: {}", exception.getMessage());
            throw new UpstreamServiceException(
                    "Request-service tidak dapat dihubungi untuk membuat laporan",
                    exception
            );
        } catch (Exception exception) {
            log.error("[REPORT] Gagal memproses respons request-service: {}", exception.getMessage());
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
            sheet.setColumnWidth(1, 40 * 256);
            sheet.setColumnWidth(2, 25 * 256);
            sheet.setColumnWidth(3, 25 * 256);

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

            sheet.setAutoFilter(
                    new org.apache.poi.ss.util.CellRangeAddress(
                            0,
                            0,
                            0,
                            headers.length - 1
                    )
            );
            sheet.createFreezePane(0, 1);

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
