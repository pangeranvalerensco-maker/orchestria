package com.pangeranvalerensco.orchestria.notification_report_service.service;

import java.util.List;

/**
 * Interface untuk layanan pengiriman email.
 *
 * Mendukung plain text dan HTML, dengan To, Cc, dan Bcc.
 * Implementasi: {@link com.pangeranvalerensco.orchestria.notification_report_service.service.impl.EmailServiceImpl}
 */
public interface EmailService {

    /**
     * Kirim email plain text.
     *
     * @param to      daftar penerima utama
     * @param subject subjek email
     * @param body    isi email plain text
     */
    void sendPlainText(List<String> to, String subject, String body);

    /**
     * Kirim email plain text dengan Cc dan Bcc.
     *
     * @param to      daftar penerima utama
     * @param cc      daftar Cc (boleh null/kosong)
     * @param bcc     daftar Bcc (boleh null/kosong)
     * @param subject subjek email
     * @param body    isi email plain text
     */
    void sendPlainText(List<String> to, List<String> cc, List<String> bcc, String subject, String body);

    /**
     * Kirim email HTML.
     *
     * @param to      daftar penerima utama
     * @param subject subjek email
     * @param htmlBody isi email dalam format HTML
     */
    void sendHtml(List<String> to, String subject, String htmlBody);

    /**
     * Kirim email HTML dengan Cc dan Bcc.
     *
     * @param to       daftar penerima utama
     * @param cc       daftar Cc (boleh null/kosong)
     * @param bcc      daftar Bcc (boleh null/kosong)
     * @param subject  subjek email
     * @param htmlBody isi email dalam format HTML
     */
    void sendHtml(List<String> to, List<String> cc, List<String> bcc, String subject, String htmlBody);
}
