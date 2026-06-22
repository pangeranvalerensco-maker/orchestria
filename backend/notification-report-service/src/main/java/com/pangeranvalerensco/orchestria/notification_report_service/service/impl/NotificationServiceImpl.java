package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationLogResponse;
import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationStatus;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.NotificationLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.service.EmailDeliveryResult;
import com.pangeranvalerensco.orchestria.notification_report_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementasi NotificationService.
 *
 * Mempublikasikan {@link NotificationEvent} melalui Spring ApplicationEventPublisher
 * sehingga dapat ditangkap oleh {@link com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEventListener}.
 *
 * Ini adalah implementasi nyata — event yang dipublikasikan diproses oleh listener
 * yang melakukan logging, audit, atau tindakan lanjutan.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final ApplicationEventPublisher eventPublisher;
    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;

    @Override
    public void publishNotification(String eventType, String message) {
        log.info("[NOTIFICATION-SERVICE] Mempublikasikan event: type={}, message={}", eventType, message);
        NotificationEvent event = new NotificationEvent(this, eventType, message);
        eventPublisher.publishEvent(event);
        log.debug("[NOTIFICATION-SERVICE] Event '{}' berhasil dipublikasikan.", eventType);
    }

    @Override
    public void sendNotification(NotificationSendRequest request, String requestedByEmail) {
        NotificationLog logEntry = NotificationLog.builder()
                .toRecipients(request.getTo())
                .ccRecipients(request.getCc())
                .bccRecipients(request.getBcc())
                .subject(request.getSubject())
                .body(request.getBody())
                .html(request.isHtml())
                .status(NotificationStatus.PENDING)
                .attemptCount(0)
                .createdByEmail(requestedByEmail)
                .build();
        
        notificationLogRepository.save(logEntry);
        
        doSendNotification(logEntry);
    }

    @Override
    public void retryNotification(String notificationId, String requestedByEmail) {
        NotificationLog logEntry = notificationLogRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification tidak ditemukan"));
                
        if (logEntry.getStatus() == NotificationStatus.SENT) {
            throw new IllegalStateException("Notification sudah terkirim");
        }
        
        doSendNotification(logEntry);
    }

    @Override
    public Page<NotificationLogResponse> getNotificationLogs(String email, NotificationStatus status, Pageable pageable) {
        if (status != null) {
            return notificationLogRepository.findByStatus(status, pageable)
                    .map(NotificationLogResponse::fromEntity);
        }
        return notificationLogRepository.findAll(pageable)
                .map(NotificationLogResponse::fromEntity);
    }
    
    @Override
    public NotificationLogResponse getNotificationLogDetail(String id, String email) {
        NotificationLog logEntry = notificationLogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification tidak ditemukan"));
        return NotificationLogResponse.fromEntity(logEntry);
    }
    
    private void doSendNotification(NotificationLog logEntry) {
        logEntry.setAttemptCount(logEntry.getAttemptCount() + 1);
        logEntry.setLastAttemptAt(LocalDateTime.now());
        
        EmailDeliveryResult result;
        if (logEntry.isHtml()) {
            result = emailService.sendHtml(
                logEntry.getToRecipients(),
                logEntry.getCcRecipients(),
                logEntry.getBccRecipients(),
                logEntry.getSubject(),
                logEntry.getBody()
            );
        } else {
            result = emailService.sendPlainText(
                logEntry.getToRecipients(),
                logEntry.getCcRecipients(),
                logEntry.getBccRecipients(),
                logEntry.getSubject(),
                logEntry.getBody()
            );
        }
        
        if (result.isSuccess()) {
            logEntry.setStatus(NotificationStatus.SENT);
            logEntry.setSentAt(LocalDateTime.now());
            logEntry.setLastError(null);
            logEntry.setNextRetryAt(null);
        } else {
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setLastError(result.getErrorMessage());
            logEntry.setNextRetryAt(LocalDateTime.now().plusMinutes(5));
        }
        
        notificationLogRepository.save(logEntry);
    }
}
