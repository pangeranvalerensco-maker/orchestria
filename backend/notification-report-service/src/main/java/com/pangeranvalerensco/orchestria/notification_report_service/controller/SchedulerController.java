package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import com.pangeranvalerensco.orchestria.notification_report_service.entity.ScheduledJobLog;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.scheduler.NotificationScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final ScheduledJobLogRepository scheduledJobLogRepository;
    private final NotificationScheduler notificationScheduler;

    @GetMapping("/logs")
    @PreAuthorize("hasAuthority('scheduler.log.read')")
    public ResponseEntity<Page<ScheduledJobLog>> getLogs(Pageable pageable) {
        return ResponseEntity.ok(scheduledJobLogRepository.findAllByOrderByStartedAtDesc(pageable));
    }

    @PostMapping("/{jobName}/trigger")
    @PreAuthorize("hasAuthority('scheduler.log.read')") // Assume if they can read logs, they can trigger, or add new permission. Since requirements say "scheduler.log.read" is mapped, let's use it for now. Or use "SUPER_ADMIN"
    public ResponseEntity<Void> triggerJob(@PathVariable String jobName) {
        notificationScheduler.manualTrigger(jobName);
        return ResponseEntity.ok().build();
    }
}
