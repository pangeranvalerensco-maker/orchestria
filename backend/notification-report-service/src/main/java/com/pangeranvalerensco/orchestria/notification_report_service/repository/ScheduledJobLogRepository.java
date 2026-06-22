package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ScheduledJobLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduledJobLogRepository extends JpaRepository<ScheduledJobLog, String> {
    Page<ScheduledJobLog> findAllByOrderByStartedAtDesc(Pageable pageable);
}
