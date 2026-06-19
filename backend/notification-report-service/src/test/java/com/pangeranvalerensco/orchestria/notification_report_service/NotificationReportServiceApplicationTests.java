package com.pangeranvalerensco.orchestria.notification_report_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Context load test — memastikan Spring context berhasil dimuat
 * tanpa koneksi eksternal (DB, SMTP, request-service).
 */
@SpringBootTest
@ActiveProfiles("test")
class NotificationReportServiceApplicationTests {

	@Test
	void contextLoads() {
		// Jika context berhasil dimuat, test ini lulus
	}

}
