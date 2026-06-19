package com.pangeranvalerensco.orchestria.notification_report_service.service.impl;

import com.pangeranvalerensco.orchestria.notification_report_service.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementasi EmailService menggunakan Spring JavaMailSender.
 *
 * Fitur:
 * - Mendukung plain text dan HTML
 * - Mendukung To, Cc, dan Bcc
 * - Konfigurasi SMTP dari environment/application-local.properties
 * - Kegagalan email dicatat dengan jelas di log (tidak melempar exception ke pemanggil)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@orchestria.local}")
    private String fromAddress;

    @Value("${app.mail.from-name:Orchestria System}")
    private String fromName;

    // =========================================================================
    //  Plain Text
    // =========================================================================

    @Override
    public void sendPlainText(List<String> to, String subject, String body) {
        sendPlainText(to, null, null, subject, body);
    }

    @Override
    public void sendPlainText(List<String> to, List<String> cc, List<String> bcc, String subject, String body) {
        log.info("[EMAIL] Mengirim plain text ke: {} | Subjek: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(String.format("%s <%s>", fromName, fromAddress));
            message.setTo(toArray(to));
            if (cc != null && !cc.isEmpty())  message.setCc(toArray(cc));
            if (bcc != null && !bcc.isEmpty()) message.setBcc(toArray(bcc));
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("[EMAIL] ✓ Plain text berhasil dikirim ke: {}", to);
        } catch (MailException e) {
            log.error("[EMAIL] ✗ GAGAL kirim plain text ke: {} | Subjek: {} | Penyebab: {}", to, subject, e.getMessage(), e);
        }
    }

    // =========================================================================
    //  HTML
    // =========================================================================

    @Override
    public void sendHtml(List<String> to, String subject, String htmlBody) {
        sendHtml(to, null, null, subject, htmlBody);
    }

    @Override
    public void sendHtml(List<String> to, List<String> cc, List<String> bcc, String subject, String htmlBody) {
        log.info("[EMAIL] Mengirim HTML ke: {} | Subjek: {}", to, subject);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromAddress, fromName);
            helper.setTo(toArray(to));
            if (cc != null && !cc.isEmpty())  helper.setCc(toArray(cc));
            if (bcc != null && !bcc.isEmpty()) helper.setBcc(toArray(bcc));
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = isHtml

            mailSender.send(mimeMessage);
            log.info("[EMAIL] ✓ HTML berhasil dikirim ke: {}", to);
        } catch (MailException e) {
            log.error("[EMAIL] ✗ GAGAL kirim HTML ke: {} | Subjek: {} | Penyebab: {}", to, subject, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[EMAIL] ✗ GAGAL kirim HTML (error tidak terduga) ke: {} | Penyebab: {}", to, e.getMessage(), e);
        }
    }

    // =========================================================================
    //  Helpers
    // =========================================================================

    private String[] toArray(List<String> list) {
        return list != null ? list.toArray(new String[0]) : new String[0];
    }
}
