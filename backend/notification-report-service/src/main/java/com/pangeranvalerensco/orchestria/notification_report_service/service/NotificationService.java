package com.pangeranvalerensco.orchestria.notification_report_service.service;

/**
 * Interface untuk layanan notifikasi internal.
 *
 * Menggunakan Spring ApplicationEventPublisher untuk mempublikasikan event
 * yang kemudian ditangani oleh NotificationEventListener.
 *
 * Implementasi: {@link com.pangeranvalerensco.orchestria.notification_report_service.service.impl.NotificationServiceImpl}
 */
public interface NotificationService {

    /**
     * Publikasikan event notifikasi dengan tipe dan pesan tertentu.
     *
     * @param eventType tipe event, misal "REPORT_READY"
     * @param message   pesan deskriptif
     */
    void publishNotification(String eventType, String message);
}
