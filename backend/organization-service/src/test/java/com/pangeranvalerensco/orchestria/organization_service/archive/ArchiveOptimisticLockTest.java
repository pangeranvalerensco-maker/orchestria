package com.pangeranvalerensco.orchestria.organization_service.archive;

import com.pangeranvalerensco.orchestria.organization_service.service.ArchiveDocumentService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Separate test class (no @Transactional) to verify that
 * ObjectOptimisticLockingFailureException from the service layer
 * is mapped to HTTP 409 by GlobalExceptionHandler.
 *
 * Uses @MockitoBean to inject a mock ArchiveDocumentService that
 * throws the exception on softDeleteDocument().
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ArchiveOptimisticLockTest {

    private static final String JWT_SECRET =
            "orchestria-test-jwt-secret-key-12345678901234567890";
    private static final String BASE_URL = "/api/organization/archive/documents";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArchiveDocumentService archiveDocumentService;

    // ── 15. Optimistic locking conflict → 409 ───────────────────────────────
    @Test
    void givenOptimisticLockingFailure_whenDelete_thenReturn409() throws Exception {
        doThrow(new ObjectOptimisticLockingFailureException(Object.class, 99L))
                .when(archiveDocumentService)
                .softDeleteDocument(anyLong(), anyString());

        String token = buildToken("admin@test.com", List.of("archive.manage"));

        mockMvc.perform(delete(BASE_URL + "/99")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Konflik data")));
    }

    private String buildToken(String email, List<String> permissions) {
        Key key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("roles", List.of())
                .claim("permissions", permissions)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }
}
