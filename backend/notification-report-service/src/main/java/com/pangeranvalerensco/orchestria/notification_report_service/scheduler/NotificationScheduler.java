package com.pangeranvalerensco.orchestria.notification_report_service.scheduler;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ScheduledJobLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.SchedulerExecutionStatus;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.SchedulerTriggerType;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationService notificationService;
    private final ScheduledJobLogRepository scheduledJobLogRepository;

    @Value("${app.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    @Scheduled(fixedRate = 300_000)
    public void healthPingScheduler() {
        executeJob("Health Ping", SchedulerTriggerType.FIXED_RATE, () -> {
            log.info("[SCHEDULER][fixedRate] ✓ Notification pipeline aktif");
            notificationService.publishNotification(
                    "SCHEDULE_TRIGGERED",
                    "Health ping (fixedRate) dieksekusi"
            );
        });
    }

    @Scheduled(fixedDelay = 600_000)
    public void retryFailedEmailScheduler() {
        executeJob("Retry Failed Email", SchedulerTriggerType.FIXED_DELAY, () -> {
            log.info("[SCHEDULER][fixedDelay] ↻ Memeriksa antrian email gagal");
            log.info("[SCHEDULER][fixedDelay] → Tidak ada email gagal dalam antrian (simulasi).");
        });
    }

    @Scheduled(cron = "${app.scheduler.cron.weekly-report:0 0 8 * * MON}")
    public void weeklyReportReminderScheduler() {
        executeJob("Weekly Report Reminder", SchedulerTriggerType.CRON, () -> {
            log.info("[SCHEDULER][cron] 📊 Weekly Report Reminder dieksekusi");
            notificationService.publishNotification(
                    "SCHEDULE_TRIGGERED",
                    "Weekly report reminder (cron) dieksekusi."
            );
        });
    }
    
    public void manualTrigger(String jobName) {
        executeJob(jobName, SchedulerTriggerType.MANUAL, () -> {
            log.info("[SCHEDULER][manual] Manual trigger eksekusi untuk: {}", jobName);
            notificationService.publishNotification(
                    "SCHEDULE_TRIGGERED",
                    "Manual trigger untuk job: " + jobName
            );
        });
    }

    private void executeJob(String jobName, SchedulerTriggerType triggerType, Runnable jobLogic) {
        if (!schedulerEnabled && triggerType != SchedulerTriggerType.MANUAL) {
            log.debug("[SCHEDULER] Scheduler dinonaktifkan, skip job: {}", jobName);
            return;
        }

        ScheduledJobLog jobLog = ScheduledJobLog.builder()
                .jobName(jobName)
                .triggerType(triggerType)
                .startedAt(LocalDateTime.now())
                .status(SchedulerExecutionStatus.SUCCESS)
                .build();

        try {
            jobLogic.run();
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
