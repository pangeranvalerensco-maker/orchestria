package com.pangeranvalerensco.orchestria.notification_report_service.service;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public interface ReportService {

    ByteArrayOutputStream generateFundRequestExcel(String authorizationHeader);

    List<FundRequestDto> fetchFundRequests(String authorizationHeader);

    Map<String, Object> getReportSummary(String authorizationHeader);

    ByteArrayOutputStream generateSubscriberTemplate();

    void importSubscribers(java.io.InputStream inputStream);

    List<com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportSubscriber> getSubscribers();

    org.springframework.data.domain.Page<com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportExportLog> getExportLogs(org.springframework.data.domain.Pageable pageable);
}
