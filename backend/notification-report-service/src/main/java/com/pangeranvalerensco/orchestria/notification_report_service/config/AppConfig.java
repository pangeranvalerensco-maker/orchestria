package com.pangeranvalerensco.orchestria.notification_report_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Konfigurasi umum aplikasi.
 *
 * Menyediakan:
 * - RestTemplate untuk komunikasi REST antar service
 * - ObjectMapper (Jackson 3.x / tools.jackson) dengan konfigurasi:
 *   - FAIL_ON_UNKNOWN_PROPERTIES = false (agar deserialisasi dari service lain toleran)
 *   - Menggunakan JsonMapper.builder() untuk konfigurasi tipe-safe di Jackson 3.x
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate digunakan oleh ReportService untuk mengambil data
     * dari request-service melalui HTTP.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper yang dikonfigurasi untuk deserialisasi JSON dari request-service.
     *
     * - Menggunakan Jackson 3.x (tools.jackson.databind.ObjectMapper)
     * - FAIL_ON_UNKNOWN_PROPERTIES = false: toleran terhadap field tambahan dari service lain
     * - Di Jackson 3.x, Java 8 date/time sudah didukung secara default (tidak perlu modul terpisah)
     */
    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .findAndAddModules()
                .build();
    }
}
