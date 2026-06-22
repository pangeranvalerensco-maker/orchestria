package com.pangeranvalerensco.orchestria.notification_report_service.service;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;
import com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.exception.UpstreamServiceException;
import com.pangeranvalerensco.orchestria.notification_report_service.service.impl.ReportServiceImpl;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) @org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class ReportServiceImplTest {

    private static final String AUTHORIZATION = "Bearer test-token";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private com.pangeranvalerensco.orchestria.notification_report_service.repository.ReportExportLogRepository reportExportLogRepository;

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
        when(reportExportLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void whenRequestServiceUnavailable_thenThrowUpstreamException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        assertThatThrownBy(() -> reportService.fetchFundRequests(AUTHORIZATION))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessageContaining("tidak dapat dihubungi");
    }

    @Test
    void whenRequestServiceReturnsNullBody_thenThrowUpstreamException() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(null));

        assertThatThrownBy(() -> reportService.fetchFundRequests(AUTHORIZATION))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessageContaining("respons kosong");
    }

    @Test
    void whenUpstreamReturnsErrorStatus_thenDoNotGenerateEmptyReport() {
        for (HttpStatus status : List.of(
                HttpStatus.UNAUTHORIZED,
                HttpStatus.FORBIDDEN,
                HttpStatus.INTERNAL_SERVER_ERROR)) {
            when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                    .thenReturn(ResponseEntity.status(status).body("{}"));

            assertThatThrownBy(() -> reportService.generateFundRequestExcel(AUTHORIZATION))
                    .isInstanceOf(UpstreamServiceException.class);
        }
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void whenFetchingData_thenAuthorizationHeaderIsForwarded() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(successResponseWithOneRecord()));

        List<FundRequestDto> result = reportService.fetchFundRequests(AUTHORIZATION);

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.GET),
                entityCaptor.capture(),
                eq(String.class)
        );

        assertThat(result).hasSize(1);
        assertThat(entityCaptor.getValue().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .isEqualTo(AUTHORIZATION);
    }

    @Test
    void whenUpstreamReturnsSuccessfulEmptyContent_thenGenerateValidExcel() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(successResponseWithEmptyContent()));

        ByteArrayOutputStream output = reportService.generateFundRequestExcel(AUTHORIZATION);

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(output.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isZero();
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(sheet.getRow(0).getCell(6).getStringCellValue())
                    .isEqualTo("Tanggal Dibuat");
        }
    }

    @Test
    void whenGenerateExcelWithData_thenDataRowsArePresent() throws Exception {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(successResponseWithOneRecord()));

        ByteArrayOutputStream output = reportService.generateFundRequestExcel(AUTHORIZATION);

        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(output.toByteArray()))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(1);

            Row dataRow = sheet.getRow(1);
            assertThat(dataRow.getCell(0).getNumericCellValue()).isEqualTo(42.0);
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("Pengadaan Projector");
            assertThat(dataRow.getCell(4).getStringCellValue()).isEqualTo("COMPLETED");
            assertThat(dataRow.getCell(5).getNumericCellValue()).isEqualTo(5000000.0);
        }
    }

    @Test
    void whenGenerateExcelSucceeds_thenReportReadyEventIsPublished() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok(successResponseWithEmptyContent()));

        reportService.generateFundRequestExcel(AUTHORIZATION);

        verify(eventPublisher, times(1)).publishEvent(any(NotificationEvent.class));
    }

    private String successResponseWithEmptyContent() {
        return """
                {
                  "success": true,
                  "message": "OK",
                  "data": {
                    "content": [],
                    "page": 0,
                    "size": 0,
                    "totalElements": 0,
                    "totalPages": 0,
                    "first": true,
                    "last": true
                  }
                }
                """;
    }

    private String successResponseWithOneRecord() {
        return """
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
    }
}
