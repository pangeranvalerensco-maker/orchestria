package com.pangeranvalerensco.orchestria.notification_report_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * Spring Application Event untuk notifikasi internal.
 *
 * Diterbitkan melalui {@link org.springframework.context.ApplicationEventPublisher}
 * dan diproses oleh {@link NotificationEventListener}.
 *
 * Contoh penggunaan:
 * <pre>
 *   eventPublisher.publishEvent(new NotificationEvent(this, "REPORT_READY", "Laporan dana siap diunduh"));
 * </pre>
 */
@Getter
public class NotificationEvent extends ApplicationEvent {

    private final String eventType;
    private final String message;
    private final LocalDateTime occurredAt;

    /**
     * @param source    objek yang mempublikasikan event (biasanya 'this')
     * @param eventType tipe event, misal "REPORT_READY", "SCHEDULE_TRIGGERED"
     * @param message   pesan deskriptif untuk event ini
     */
    public NotificationEvent(Object source, String eventType, String message) {
        super(source);
        this.eventType   = eventType;
        this.message     = message;
        this.occurredAt  = LocalDateTime.now();
    }
}
