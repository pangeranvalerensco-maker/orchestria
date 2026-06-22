package com.pangeranvalerensco.orchestria.notification_report_service.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReportGeneratedEvent extends ApplicationEvent {
    
    private final String reportType;
    private final String filename;
    private final String requestedByEmail;

    public ReportGeneratedEvent(Object source, String reportType, String filename, String requestedByEmail) {
        super(source);
        this.reportType = reportType;
        this.filename = filename;
        this.requestedByEmail = requestedByEmail;
    }
}
