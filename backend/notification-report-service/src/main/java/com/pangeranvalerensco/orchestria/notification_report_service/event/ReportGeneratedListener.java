package com.pangeranvalerensco.orchestria.notification_report_service.event;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportGeneratedListener {

    private final NotificationService notificationService;

    @EventListener
    public void onReportGenerated(ReportGeneratedEvent event) {
        log.info("[EVENT-LISTENER] Menerima event laporan: tipe={}, filename={}, oleh={}", 
            event.getReportType(), event.getFilename(), event.getRequestedByEmail());
        
        NotificationSendRequest request = new NotificationSendRequest();
        request.setTo(List.of(event.getRequestedByEmail()));
        request.setSubject("Laporan " + event.getReportType() + " telah di-generate");
        request.setBody("Laporan Anda " + event.getFilename() + " sudah selesai dibuat. Silakan periksa sistem.");
        request.setHtml(false);
        
        try {
            notificationService.sendNotification(request, "system@orchestria.local");
            log.info("[EVENT-LISTENER] Notifikasi email telah di-enqueue untuk {}", event.getRequestedByEmail());
        } catch (Exception e) {
            log.error("[EVENT-LISTENER] Gagal men-enqueue notifikasi: {}", e.getMessage(), e);
        }
    }
}
