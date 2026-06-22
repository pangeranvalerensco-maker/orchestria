package com.pangeranvalerensco.orchestria.notification_report_service.event;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGeneratedListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleReportGeneratedEvent(ReportGeneratedEvent event) {
        log.info("[REPORT-LISTENER] Menerima event laporan dibuat: {}", event.getFilename());
        
        NotificationSendRequest request = new NotificationSendRequest();
        request.setTo(List.of(event.getRequestedByEmail()));
        request.setSubject("Laporan Anda Telah Selesai: " + event.getFilename());
        
        String body = String.format(
            "<h1>Laporan Selesai</h1><p>Laporan %s berhasil dibuat dengan %d record pada %s.</p>",
            event.getFilename(),
            event.getRecordCount(),
            event.getOccurredAt().toString()
        );
        request.setBody(body);
        request.setHtml(true);

        try {
            notificationService.sendNotification(request, "system");
        } catch (Exception e) {
            log.error("[REPORT-LISTENER] Gagal mengirim notifikasi laporan selesai", e);
        }
    }
}
