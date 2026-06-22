package com.pangeranvalerensco.orchestria.notification_report_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

@Getter
public class ReportGeneratedEvent extends ApplicationEvent {
    private final String requestedByEmail;
    private final String filename;
    private final int recordCount;
    private final LocalDateTime occurredAt;

    public ReportGeneratedEvent(Object source, String requestedByEmail, String filename, int recordCount, LocalDateTime occurredAt) {
        super(source);
        this.requestedByEmail = requestedByEmail;
        this.filename = filename;
        this.recordCount = recordCount;
        this.occurredAt = occurredAt;
    }
}
