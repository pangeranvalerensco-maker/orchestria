package com.pangeranvalerensco.orchestria.notification_report_service.service;

import java.io.ByteArrayOutputStream;
import java.util.List;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.FundRequestDto;

/**
 * Interface untuk layanan pembuatan laporan.
 *
 * Implementasi: {@link com.pangeranvalerensco.orchestria.notification_report_service.service.impl.ReportServiceImpl}
 */
public interface ReportService {

    /**
     * Mengambil semua data fund request dari request-service
     * dan menghasilkan file Excel (.xlsx) dalam bentuk byte array.
     *
     * @return ByteArrayOutputStream berisi file Excel siap unduh
     */
    ByteArrayOutputStream generateFundRequestExcel();

    /**
     * Mengambil data fund request dari request-service.
     * Dapat dipanggil secara terpisah untuk keperluan lain.
     *
     * @return list fund request dari request-service
     */
    List<FundRequestDto> fetchFundRequests();
}
