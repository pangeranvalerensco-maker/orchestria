package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ScheduledJobLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.SchedulerExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ScheduledJobLogRepository extends JpaRepository<ScheduledJobLog, String> {
    Page<ScheduledJobLog> findAllByOrderByStartedAtDesc(Pageable pageable);
    List<ScheduledJobLog> findByStatus(SchedulerExecutionStatus status);
}
