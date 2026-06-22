package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
    Page<NotificationLog> findByCreatedByEmail(String email, Pageable pageable);
    List<NotificationLog> findByStatus(NotificationStatus status);
}
