package com.pangeranvalerensco.orchestria.notification_report_service.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ReportSecurityIntegrationTest {

    private static final String SECRET =
            "orchestria-test-jwt-secret-key-minimum-32-characters-long";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void healthEndpointWithoutTokenIsAccessible() throws Exception {
        mockMvc.perform(get("/api/notifications/health"))
                .andExpect(status().isOk());
    }

    @Test
    void reportEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/fund-requests.xlsx"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reportEndpointWithoutRequiredPermissionIsForbidden() throws Exception {
        mockMvc.perform(get("/api/reports/fund-requests.xlsx")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenWithPermissions(List.of("request.read.own"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportEndpointWithRequiredPermissionPassesSecurityLayer() throws Exception {
        mockMvc.perform(get("/api/reports/fund-requests.xlsx")
                        .header(HttpHeaders.AUTHORIZATION,
                                "Bearer " + tokenWithPermissions(List.of("report.export"))))
                .andExpect(status().isBadGateway());
    }

    private String tokenWithPermissions(List<String> permissions) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 60_000);

        return Jwts.builder()
                .subject("auditor@orchestria.local")
                .claim("userId", 99L)
                .claim("fullName", "Report Auditor")
                .claim("roles", List.of("BENDAHARA_INTERNAL"))
                .claim("permissions", permissions)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }
}
