package com.pangeranvalerensco.orchestria.notification_report_service.service;

/**
 * Interface untuk layanan notifikasi internal.
 *
 * Menggunakan Spring ApplicationEventPublisher untuk mempublikasikan event
 * yang kemudian ditangani oleh NotificationEventListener.
 *
 * Implementasi: {@link com.pangeranvalerensco.orchestria.notification_report_service.service.impl.NotificationServiceImpl}
 */
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void publishNotification(String eventType, String message);

    void sendNotification(NotificationSendRequest request, String requestedByEmail);

    void retryNotification(String notificationId, String requestedByEmail);

    Page<NotificationLogResponse> getNotificationLogs(String email, NotificationStatus status, Pageable pageable);

    NotificationLogResponse getNotificationLogDetail(String id, String email);
}
