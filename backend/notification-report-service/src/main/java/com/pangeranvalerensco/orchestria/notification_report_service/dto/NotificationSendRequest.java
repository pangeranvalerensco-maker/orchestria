package com.pangeranvalerensco.orchestria.notification_report_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class NotificationSendRequest {
    @NotEmpty(message = "Penerima tidak boleh kosong")
    private List<String> to;
    private List<String> cc;
    private List<String> bcc;
    
    @NotBlank(message = "Subjek tidak boleh kosong")
    private String subject;
    
    @NotBlank(message = "Body tidak boleh kosong")
    private String body;
    
    private boolean html;
}
