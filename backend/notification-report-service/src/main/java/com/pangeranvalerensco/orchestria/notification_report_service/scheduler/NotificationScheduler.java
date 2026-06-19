package com.pangeranvalerensco.orchestria.notification_report_service.scheduler;

import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scheduler untuk tugas-tugas berkala notification-report-service.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  PENJELASAN TIPE SCHEDULER
 * ─────────────────────────────────────────────────────────────────────────────
 *
 *  @Scheduled(fixedRate = N)
 *    → Dieksekusi setiap N milidetik dari AWAL eksekusi terakhir.
 *    → Cocok untuk monitoring periodic yang tidak peduli durasi task.
 *    → Contoh: cek status sistem setiap 60 detik.
 *
 *  @Scheduled(fixedDelay = N)
 *    → Dieksekusi N milidetik setelah SELESAI eksekusi terakhir.
 *    → Cocok untuk task yang harus selesai sebelum dimulai kembali.
 *    → Contoh: kirim ulang antrian email yang belum terkirim.
 *
 *  @Scheduled(cron = "...")
 *    → Dieksekusi berdasarkan ekspresi cron 6-field (detik menit jam hari bulan hari-minggu).
 *    → Cocok untuk task berbasis waktu kalender spesifik.
 *    → Contoh: laporan harian setiap hari Senin pukul 08:00.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 *  MENONAKTIFKAN SCHEDULER
 * ─────────────────────────────────────────────────────────────────────────────
 *  Semua task scheduler dapat dinonaktifkan dengan property:
 *    app.scheduler.enabled=false
 *
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // =========================================================================
    //  1. fixedRate — Health ping setiap 5 menit
    //     Dieksekusi setiap 5 menit dari WAKTU MULAI eksekusi sebelumnya.
    //     Contoh nyata: sistem mengecek apakah notification pipeline masih aktif.
    // =========================================================================

    @Scheduled(fixedRate = 300_000) // 5 menit
    public void healthPingScheduler() {
        if (!schedulerEnabled) {
            log.debug("[SCHEDULER][fixedRate] Scheduler dinonaktifkan, skip health ping.");
            return;
        }
        String waktu = LocalDateTime.now().format(FORMATTER);
        log.info("[SCHEDULER][fixedRate] ✓ Notification pipeline aktif | Waktu: {}", waktu);

        // Publikasikan event ke pipeline notifikasi
        notificationService.publishNotification(
                "SCHEDULE_TRIGGERED",
                "Health ping (fixedRate) dieksekusi pada " + waktu
        );
    }

    // =========================================================================
    //  2. fixedDelay — Retry antrian email gagal setiap 10 menit
    //     Dieksekusi 10 menit SETELAH task sebelumnya selesai.
    //     Cocok agar tidak ada dua proses retry berjalan bersamaan.
    // =========================================================================

    @Scheduled(fixedDelay = 600_000) // 10 menit
    public void retryFailedEmailScheduler() {
        if (!schedulerEnabled) {
            log.debug("[SCHEDULER][fixedDelay] Scheduler dinonaktifkan, skip retry email.");
            return;
        }
        String waktu = LocalDateTime.now().format(FORMATTER);
        log.info("[SCHEDULER][fixedDelay] ↻ Memeriksa antrian email gagal | Waktu: {}", waktu);

        // Dalam implementasi nyata: ambil email gagal dari queue/DB dan retry
        // Saat ini: log saja sebagai bukti fixedDelay scheduler berjalan
        log.info("[SCHEDULER][fixedDelay] → Tidak ada email gagal dalam antrian (simulasi).");
    }

    // =========================================================================
    //  3. cron — Laporan harian setiap Senin pukul 08:00
    //     Format cron Spring: detik menit jam hari-bulan bulan hari-minggu
    //     "0 0 8 * * MON" = setiap Senin jam 08:00:00
    // =========================================================================

    @Scheduled(cron = "${app.scheduler.cron.weekly-report:0 0 8 * * MON}")
    public void weeklyReportReminderScheduler() {
        if (!schedulerEnabled) {
            log.debug("[SCHEDULER][cron] Scheduler dinonaktifkan, skip weekly report reminder.");
            return;
        }
        String waktu = LocalDateTime.now().format(FORMATTER);
        log.info("[SCHEDULER][cron] 📊 Weekly Report Reminder dieksekusi | Waktu: {}", waktu);

        // Dalam implementasi nyata: trigger generate laporan Excel dan kirim ke email stakeholder
        // Saat ini: publikasikan event saja (tidak kirim email nyata)
        notificationService.publishNotification(
                "SCHEDULE_TRIGGERED",
                "Weekly report reminder (cron) dieksekusi pada " + waktu
                + ". Laporan siap dibuat melalui GET /api/reports/fund-requests.xlsx"
        );

        log.info("[SCHEDULER][cron] ✓ Event weekly report reminder berhasil dipublikasikan.");
    }
}
