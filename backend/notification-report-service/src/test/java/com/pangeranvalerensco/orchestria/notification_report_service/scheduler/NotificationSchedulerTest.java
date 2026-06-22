package com.pangeranvalerensco.orchestria.notification_report_service.scheduler;

import com.pangeranvalerensco.orchestria.notification_report_service.dto.NotificationSendRequest;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.NotificationLog;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportSubscriber;
import com.pangeranvalerensco.orchestria.notification_report_service.entity.ReportType;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.NotificationLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ReportSubscriberRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository;
import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class NotificationSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ScheduledJobLogRepository scheduledJobLogRepository;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private ReportSubscriberRepository reportSubscriberRepository;

    @InjectMocks
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(scheduledJobLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void whenSchedulerDisabled_healthPing_doesNotPublishEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);
        assertThatCode(() -> scheduler.healthPingScheduler()).doesNotThrowAnyException();
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerDisabled_retryEmail_doesNothing() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);
        assertThatCode(() -> scheduler.retryFailedEmailScheduler()).doesNotThrowAnyException();
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerDisabled_weeklyReport_doesNotPublishEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);
        assertThatCode(() -> scheduler.weeklyReportReminderScheduler()).doesNotThrowAnyException();
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerEnabled_healthPing_publishesEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        scheduler.healthPingScheduler();
        verify(notificationService, times(1)).publishNotification(eq("SCHEDULE_HEARTBEAT"), anyString());
    }

    @Test
    void whenSchedulerEnabled_retryEmail_runsWithoutError() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        
        NotificationLog logEntry = new NotificationLog();
        logEntry.setId("test-id");
        when(notificationLogRepository.findDueRetries(anyInt(), any())).thenReturn(List.of(logEntry));

        assertThatCode(() -> scheduler.retryFailedEmailScheduler()).doesNotThrowAnyException();

        verify(notificationService, times(1)).retryNotification(eq("test-id"), eq("scheduler"));
    }

    @Test
    void whenSchedulerEnabled_weeklyReport_publishesEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);
        
        ReportSubscriber subscriber = ReportSubscriber.builder()
            .email("test@example.com")
            .active(true)
            .reportType(ReportType.WEEKLY_REQUEST_REPORT)
            .build();
        when(reportSubscriberRepository.findByActiveTrueAndReportType(ReportType.WEEKLY_REQUEST_REPORT))
            .thenReturn(List.of(subscriber));

        scheduler.weeklyReportReminderScheduler();

        verify(notificationService, times(1)).sendNotification(any(NotificationSendRequest.class), eq("scheduler"));
    }
}
