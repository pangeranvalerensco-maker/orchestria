package com.pangeranvalerensco.orchestria.notification_report_service.service;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;

import java.io.ByteArrayOutputStream;
import java.util.List;

public interface ReportService {

    ByteArrayOutputStream generateFundRequestExcel(String authorizationHeader);

    List<FundRequestDto> fetchFundRequests(String authorizationHeader);
}
