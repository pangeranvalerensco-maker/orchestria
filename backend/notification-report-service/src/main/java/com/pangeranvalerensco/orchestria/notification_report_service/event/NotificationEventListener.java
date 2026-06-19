package com.pangeranvalerensco.orchestria.notification_report_service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener untuk {@link NotificationEvent}.
 *
 * Menangkap event yang dipublikasikan oleh NotificationService dan
 * melakukan tindakan nyata: mencatat log detail, dan dapat diperluas
 * untuk mengirim notifikasi email atau tindakan lain.
 *
 * Ini adalah contoh nyata penggunaan Spring ApplicationEventPublisher +
 * @EventListener dalam satu alur kerja service.
 */
@Slf4j
@Component
public class NotificationEventListener {

    /**
     * Menangani NotificationEvent secara asinkron.
     *
     * @param event event notifikasi yang diterima
     */
    @EventListener
    public void onNotificationEvent(NotificationEvent event) {
        log.info("[EVENT-LISTENER] Menerima event notifikasi:");
        log.info("  → Tipe    : {}", event.getEventType());
        log.info("  → Pesan   : {}", event.getMessage());
        log.info("  → Waktu   : {}", event.getOccurredAt());
        log.info("  → Sumber  : {}", event.getSource().getClass().getSimpleName());

        handleEvent(event);
    }

    /**
     * Logika penanganan event berdasarkan tipe.
     * Dapat diperluas untuk mengirim email, push notification, dsb.
     */
    private void handleEvent(NotificationEvent event) {
        switch (event.getEventType()) {
            case "REPORT_READY" -> log.info(
                "[EVENT-LISTENER] Laporan siap. Tindakan: notifikasi stakeholder bahwa file Excel tersedia.");
            case "SCHEDULE_TRIGGERED" -> log.info(
                "[EVENT-LISTENER] Scheduler terpicu. Tindakan: catat aktivitas jadwal ke audit log.");
            case "EMAIL_FAILED" -> log.warn(
                "[EVENT-LISTENER] Pengiriman email gagal. Tindakan: tandai untuk percobaan ulang manual. Detail: {}",
                event.getMessage());
            default -> log.info(
                "[EVENT-LISTENER] Event generik '{}' diterima. Tidak ada tindakan khusus.", event.getEventType());
        }
    }
}
