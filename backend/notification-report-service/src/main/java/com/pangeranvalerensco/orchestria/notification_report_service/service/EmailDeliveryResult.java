package com.pangeranvalerensco.orchestria.notification_report_service.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailDeliveryResult {
    private final boolean success;
    private final String errorMessage;

    public static EmailDeliveryResult success() {
        return new EmailDeliveryResult(true, null);
    }

    public static EmailDeliveryResult failure(String errorMessage) {
        return new EmailDeliveryResult(false, errorMessage);
    }
}
