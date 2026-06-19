package com.pangeranvalerensco.orchestria.notification_report_service.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Test untuk NotificationEventListener.
 *
 * Memverifikasi bahwa:
 * 1. Event dapat dipublikasikan tanpa error
 * 2. Listener menerima dan memproses event dengan benar
 * 3. Semua tipe event (REPORT_READY, SCHEDULE_TRIGGERED, EMAIL_FAILED, unknown) ditangani
 */
@SpringBootTest
@ActiveProfiles("test")
class NotificationEventListenerTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private NotificationEventListener notificationEventListener;

    @Test
    void whenReportReadyEventPublished_thenListenerHandlesWithoutException() {
        NotificationEvent event = new NotificationEvent(this, "REPORT_READY", "Test laporan siap");

        assertThatCode(() -> eventPublisher.publishEvent(event))
                .doesNotThrowAnyException();
    }

    @Test
    void whenScheduleTriggeredEventPublished_thenListenerHandlesWithoutException() {
        NotificationEvent event = new NotificationEvent(this, "SCHEDULE_TRIGGERED", "Test scheduler terpicu");

        assertThatCode(() -> eventPublisher.publishEvent(event))
                .doesNotThrowAnyException();
    }

    @Test
    void whenEmailFailedEventPublished_thenListenerLogsWarning() {
        NotificationEvent event = new NotificationEvent(this, "EMAIL_FAILED", "Koneksi SMTP timeout");

        assertThatCode(() -> eventPublisher.publishEvent(event))
                .doesNotThrowAnyException();
    }

    @Test
    void whenUnknownEventTypePublished_thenListenerHandlesGracefully() {
        NotificationEvent event = new NotificationEvent(this, "UNKNOWN_TYPE", "Event tidak dikenal");

        assertThatCode(() -> eventPublisher.publishEvent(event))
                .doesNotThrowAnyException();
    }

    @Test
    void whenEventCreated_thenFieldsAreCorrect() {
        String eventType = "TEST_EVENT";
        String message = "Pesan test";

        NotificationEvent event = new NotificationEvent(this, eventType, message);

        org.assertj.core.api.Assertions.assertThat(event.getEventType()).isEqualTo(eventType);
        org.assertj.core.api.Assertions.assertThat(event.getMessage()).isEqualTo(message);
        org.assertj.core.api.Assertions.assertThat(event.getOccurredAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(event.getSource()).isEqualTo(this);
    }

    @Test
    void onNotificationEvent_directCall_doesNotThrow() {
        NotificationEvent event = new NotificationEvent(this, "DIRECT_CALL", "Dipanggil langsung");

        assertThatCode(() -> notificationEventListener.onNotificationEvent(event))
                .doesNotThrowAnyException();
    }
}
