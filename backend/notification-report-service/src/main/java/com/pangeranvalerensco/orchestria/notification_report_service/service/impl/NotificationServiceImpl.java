package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import com.pangeranvalerensco.orchestria.notification_report_service.event.NotificationEvent;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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

    @Override
    public void publishNotification(String eventType, String message) {
        log.info("[NOTIFICATION-SERVICE] Mempublikasikan event: type={}, message={}", eventType, message);
        NotificationEvent event = new NotificationEvent(this, eventType, message);
        eventPublisher.publishEvent(event);
        log.debug("[NOTIFICATION-SERVICE] Event '{}' berhasil dipublikasikan.", eventType);
    }
}
