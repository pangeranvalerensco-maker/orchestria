package com.pangeranvalerensco.orchestria.notification_report_service.scheduler;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportSubscriber;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportType;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.ScheduledJobLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.SchedulerExecutionStatus;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.SchedulerTriggerType;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.NotificationLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ReportSubscriberRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final ScheduledJobLogRepository scheduledJobLogRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final ReportSubscriberRepository reportSubscriberRepository;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Value("${app.scheduler.max-email-attempts:3}")
    private int maxEmailAttempts;

    @Scheduled(fixedRate = 300_000)
    public void healthPingScheduler() {
        if (!schedulerEnabled) return;
        String jobName = "Health Ping";
        ScheduledJobLog jobLog = ScheduledJobLog.builder()
                .jobName(jobName)
                .triggerType(SchedulerTriggerType.FIXED_RATE)
                .startedAt(LocalDateTime.now())
                .status(SchedulerExecutionStatus.SUCCESS)
                .build();

        try {
            log.info("[SCHEDULER][fixedRate] Notification pipeline aktif");
            notificationService.publishNotification(
                    "SCHEDULE_HEARTBEAT",
                    "Health ping (fixedRate) dieksekusi"
            );
            jobLog.setMessage("Job executed successfully");
        } catch (Exception e) {
            log.error("[SCHEDULER] Error executing job {}: {}", jobName, e.getMessage(), e);
            jobLog.setStatus(SchedulerExecutionStatus.FAILED);
            jobLog.setErrorMessage(e.getMessage());
        } finally {
            jobLog.setFinishedAt(LocalDateTime.now());
            scheduledJobLogRepository.save(jobLog);
        }
    }

    @Scheduled(fixedDelay = 600_000)
    public void retryFailedEmailScheduler() {
        if (!schedulerEnabled) return;
        String jobName = "Retry Failed Email";
        ScheduledJobLog jobLog = ScheduledJobLog.builder()
                .jobName(jobName)
                .triggerType(SchedulerTriggerType.FIXED_DELAY)
                .startedAt(LocalDateTime.now())
                .status(SchedulerExecutionStatus.SUCCESS)
                .build();

        try {
            log.info("[SCHEDULER][fixedDelay] Memeriksa antrian email gagal");
            List<NotificationLog> dueRetries = notificationLogRepository.findDueRetries(maxEmailAttempts, LocalDateTime.now());
            
            if (dueRetries.isEmpty()) {
                jobLog.setMessage("tidak ada notifikasi retry due");
                log.info("[SCHEDULER][fixedDelay] Tidak ada email gagal dalam antrian.");
            } else {
                int processed = 0;
                for (NotificationLog logEntry : dueRetries) {
                    try {
                        notificationService.retryNotification(logEntry.getId(), "scheduler");
                        processed++;
                    } catch (Exception e) {
                        log.error("[SCHEDULER][fixedDelay] Gagal retry notifikasi ID: {}", logEntry.getId(), e);
                    }
                }
                jobLog.setMessage("Berhasil memproses " + processed + " notifikasi.");
            }
        } catch (Exception e) {
            log.error("[SCHEDULER] Error executing job {}: {}", jobName, e.getMessage(), e);
            jobLog.setStatus(SchedulerExecutionStatus.FAILED);
            jobLog.setErrorMessage(e.getMessage());
        } finally {
            jobLog.setFinishedAt(LocalDateTime.now());
            scheduledJobLogRepository.save(jobLog);
        }
    }

    @Scheduled(cron = "${app.scheduler.cron.weekly-report:0 0 8 * * MON}")
    public void weeklyReportReminderScheduler() {
        if (!schedulerEnabled) return;
        String jobName = "Weekly Report Reminder";
        ScheduledJobLog jobLog = ScheduledJobLog.builder()
                .jobName(jobName)
                .triggerType(SchedulerTriggerType.CRON)
                .startedAt(LocalDateTime.now())
                .status(SchedulerExecutionStatus.SUCCESS)
                .build();

        try {
            log.info("[SCHEDULER][cron] Weekly Report Reminder dieksekusi");
            
            List<ReportSubscriber> subscribers = reportSubscriberRepository.findByActiveTrueAndReportType(ReportType.WEEKLY_REQUEST_REPORT);
            
            if (subscribers.isEmpty()) {
                jobLog.setMessage("tidak ada subscriber aktif");
            } else {
                List<String> emails = subscribers.stream()
                        .map(ReportSubscriber::getEmail)
                        .collect(Collectors.toList());
                
                NotificationSendRequest request = new NotificationSendRequest();
                request.setTo(emails);
                request.setSubject("Weekly Request Report Reminder");
                request.setBody("<h1>Weekly Report Reminder</h1><p>This is your weekly reminder.</p>");
                request.setHtml(true);
                
                notificationService.sendNotification(request, "scheduler");
                
                jobLog.setMessage("Berhasil mengirim ke " + subscribers.size() + " subscriber.");
            }
        } catch (Exception e) {
            log.error("[SCHEDULER] Error executing job {}: {}", jobName, e.getMessage(), e);
            jobLog.setStatus(SchedulerExecutionStatus.FAILED);
            jobLog.setErrorMessage(e.getMessage());
        } finally {
            jobLog.setFinishedAt(LocalDateTime.now());
            scheduledJobLogRepository.save(jobLog);
        }
    }
    
    public void manualTrigger(String jobName) {
        ScheduledJobLog jobLog = ScheduledJobLog.builder()
                .jobName(jobName)
                .triggerType(SchedulerTriggerType.MANUAL)
                .startedAt(LocalDateTime.now())
                .status(SchedulerExecutionStatus.SUCCESS)
                .build();

        try {
            log.info("[SCHEDULER][manual] Manual trigger eksekusi untuk: {}", jobName);
            notificationService.publishNotification(
                    "SCHEDULE_TRIGGERED",
                    "Manual trigger untuk job: " + jobName
            );
            jobLog.setMessage("Job executed successfully");
        } catch (Exception e) {
            log.error("[SCHEDULER] Error executing job {}: {}", jobName, e.getMessage(), e);
            jobLog.setStatus(SchedulerExecutionStatus.FAILED);
            jobLog.setErrorMessage(e.getMessage());
        } finally {
            jobLog.setFinishedAt(LocalDateTime.now());
            scheduledJobLogRepository.save(jobLog);
        }
    }
}
