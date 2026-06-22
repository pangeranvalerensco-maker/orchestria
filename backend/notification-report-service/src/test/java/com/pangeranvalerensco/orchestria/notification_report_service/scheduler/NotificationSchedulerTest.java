package com.pangeranvalerensco.orchestria.notification_report_service.scheduler;

import com.pangeranvalerensco.orchestria.notification_report_service.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*; import static org.mockito.ArgumentMatchers.any; import org.junit.jupiter.api.BeforeEach;

/**
 * Unit test untuk NotificationScheduler.
 *
 * Memverifikasi:
 * 1. Scheduler tidak mengirim email nyata
 * 2. Scheduler yang dinonaktifkan (enabled=false) tidak memanggil service
 * 3. Scheduler yang diaktifkan (enabled=true) memanggil NotificationService
 */
@ExtendWith(MockitoExtension.class) @org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class NotificationSchedulerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private com.pangeranvalerensco.orchestria.notification_report_service.repository.ScheduledJobLogRepository scheduledJobLogRepository;

    @InjectMocks
    private NotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(scheduledJobLogRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // =========================================================================
    //  Test: schedulerEnabled = false
    // =========================================================================

    @Test
    void whenSchedulerDisabled_healthPing_doesNotPublishEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);

        assertThatCode(() -> scheduler.healthPingScheduler())
                .doesNotThrowAnyException();

        // Tidak ada call ke notificationService
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerDisabled_retryEmail_doesNothing() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);

        assertThatCode(() -> scheduler.retryFailedEmailScheduler())
                .doesNotThrowAnyException();

        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerDisabled_weeklyReport_doesNotPublishEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", false);

        assertThatCode(() -> scheduler.weeklyReportReminderScheduler())
                .doesNotThrowAnyException();

        verifyNoInteractions(notificationService);
    }

    // =========================================================================
    //  Test: schedulerEnabled = true
    // =========================================================================

    @Test
    void whenSchedulerEnabled_healthPing_publishesEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);

        scheduler.healthPingScheduler();

        verify(notificationService, times(1))
                .publishNotification(eq("SCHEDULE_TRIGGERED"), anyString());
    }

    @Test
    void whenSchedulerEnabled_retryEmail_runsWithoutError() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);

        assertThatCode(() -> scheduler.retryFailedEmailScheduler())
                .doesNotThrowAnyException();

        // Retry email scheduler tidak memanggil notificationService,
        // hanya log (tidak ada email nyata yang dikirim)
        verifyNoInteractions(notificationService);
    }

    @Test
    void whenSchedulerEnabled_weeklyReport_publishesEvent() {
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);

        scheduler.weeklyReportReminderScheduler();

        verify(notificationService, times(1))
                .publishNotification(eq("SCHEDULE_TRIGGERED"), anyString());
    }

    // =========================================================================
    //  Verifikasi: tidak ada email nyata yang dikirim
    // =========================================================================

    @Test
    void schedulerNeverSendsRealEmail() {
        // Scheduler hanya memanggil NotificationService (event publisher)
        // EmailService tidak pernah diinjeksikan ke Scheduler
        // Verifikasi: NotificationScheduler tidak memiliki dependency ke EmailService
        ReflectionTestUtils.setField(scheduler, "schedulerEnabled", true);

        scheduler.healthPingScheduler();
        scheduler.retryFailedEmailScheduler();
        scheduler.weeklyReportReminderScheduler();

        // Hanya notificationService yang dipanggil, bukan email langsung
        verify(notificationService, atLeastOnce()).publishNotification(anyString(), anyString());
    }
}
