package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportExportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportExportLogRepository extends JpaRepository<ReportExportLog, String> {
    Page<ReportExportLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
