package com.pangeranvalerensco.orchestria.notification_report_service.service;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;
import com.pangeranvalerensco.orchestria.notification_report_service.service.impl.ReportServiceImpl;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test untuk ReportServiceImpl.
 *
 * Memverifikasi:
 * 1. Excel dihasilkan dengan benar dari data yang diberikan
 * 2. Header Excel mengandung kolom yang diharapkan
 * 3. Baris data terisi dengan benar
 * 4. Ketika request-service tidak tersedia, kembalikan list kosong (graceful)
 * 5. Event REPORT_READY dipublikasikan setelah Excel selesai dibuat
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Spy
    private ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .findAndAddModules()
            .build();

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reportService, "requestServiceBaseUrl", "http://localhost:8099");
    }

    // =========================================================================
    //  Test: Fetch Fund Requests
    // =========================================================================

    @Test
    void whenRequestServiceUnavailable_thenFetchReturnsEmptyList() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        List<FundRequestDto> result = reportService.fetchFundRequests();

        assertThat(result).isEmpty();
    }

    @Test
    void whenRequestServiceReturnsNull_thenFetchReturnsEmptyList() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(null));

        List<FundRequestDto> result = reportService.fetchFundRequests();

        assertThat(result).isEmpty();
    }

    @Test
    void whenRequestServiceReturnsValidData_thenFetchReturnsRecords() throws Exception {
        String mockJson = """
                {
                  "success": true,
                  "message": "OK",
                  "data": {
                    "content": [
                      {
                        "id": 1,
                        "title": "Test Request",
                        "divisionName": "Keuangan",
                        "requesterName": "Budi",
                        "status": "APPROVED",
                        "totalAmount": 1500000.00,
                        "createdAt": "2026-06-01T10:00:00"
                      }
                    ],
                    "page": 0,
                    "size": 1,
                    "totalElements": 1,
                    "totalPages": 1,
                    "first": true,
                    "last": true
                  }
                }
                """;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockJson));

        List<FundRequestDto> result = reportService.fetchFundRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getTitle()).isEqualTo("Test Request");
        assertThat(result.get(0).getDivisionName()).isEqualTo("Keuangan");
        assertThat(result.get(0).getStatus()).isEqualTo("APPROVED");
    }

    // =========================================================================
    //  Test: Excel Generator
    // =========================================================================

    @Test
    void whenGenerateExcelWithEmptyData_thenOnlyHeaderRow() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(null));

        ByteArrayOutputStream out = reportService.generateFundRequestExcel();

        assertThat(out).isNotNull();
        assertThat(out.size()).isGreaterThan(0);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getSheetName()).isEqualTo("Fund Requests");
            assertThat(sheet.getLastRowNum()).isEqualTo(0);

            Row headerRow = sheet.getRow(0);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(headerRow.getCell(1).getStringCellValue()).isEqualTo("Judul");
            assertThat(headerRow.getCell(2).getStringCellValue()).isEqualTo("Divisi");
            assertThat(headerRow.getCell(3).getStringCellValue()).isEqualTo("Pemohon");
            assertThat(headerRow.getCell(4).getStringCellValue()).isEqualTo("Status");
            assertThat(headerRow.getCell(5).getStringCellValue()).isEqualTo("Total (Rp)");
            assertThat(headerRow.getCell(6).getStringCellValue()).isEqualTo("Tanggal Dibuat");
        }
    }

    @Test
    void whenGenerateExcelWithData_thenDataRowsPresent() throws Exception {
        String mockJson = """
                {
                  "success": true,
                  "message": "OK",
                  "data": {
                    "content": [
                      {
                        "id": 42,
                        "title": "Pengadaan Projector",
                        "divisionName": "IT",
                        "requesterName": "Siti",
                        "status": "COMPLETED",
                        "totalAmount": 5000000.00,
                        "createdAt": "2026-05-15T09:30:00"
                      }
                    ],
                    "page": 0,
                    "size": 1,
                    "totalElements": 1,
                    "totalPages": 1,
                    "first": true,
                    "last": true
                  }
                }
                """;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(mockJson));

        ByteArrayOutputStream out = reportService.generateFundRequestExcel();

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(1);

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getNumericCellValue()).isEqualTo(42.0);
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Pengadaan Projector");
            assertThat(dataRow.getCell(2).getStringCellValue()).isEqualTo("IT");
            assertThat(dataRow.getCell(3).getStringCellValue()).isEqualTo("Siti");
            assertThat(dataRow.getCell(4).getStringCellValue()).isEqualTo("COMPLETED");
            assertThat(dataRow.getCell(5).getNumericCellValue()).isEqualTo(5000000.0);
        }
    }

    @Test
    void whenGenerateExcel_thenReportReadyEventPublished() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(null));

        reportService.generateFundRequestExcel();

        verify(eventPublisher, times(1)).publishEvent(
                argThat(event -> event instanceof com.pangeranvalerensco.orchestria
                        .notification_report_service.event.NotificationEvent notifEvent
                        && "REPORT_READY".equals(notifEvent.getEventType()))
        );
    }
}
