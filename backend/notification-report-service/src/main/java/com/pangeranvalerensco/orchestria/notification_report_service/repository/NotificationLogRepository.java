package com.pangeranvalerensco.orchestria.notification_report_service.repository;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, String> {
    Page<NotificationLog> findByCreatedByEmail(String email, Pageable pageable);
    
    Page<NotificationLog> findByStatus(NotificationStatus status, Pageable pageable);
    
    List<NotificationLog> findByStatus(NotificationStatus status);
    
    @Query("SELECT n FROM NotificationLog n WHERE n.status IN ('PENDING', 'FAILED') AND n.attemptCount < :maxAttempts AND (n.nextRetryAt IS NULL OR n.nextRetryAt <= :now)")
    List<NotificationLog> findDueRetries(@Param("maxAttempts") int maxAttempts, @Param("now") LocalDateTime now);
}
