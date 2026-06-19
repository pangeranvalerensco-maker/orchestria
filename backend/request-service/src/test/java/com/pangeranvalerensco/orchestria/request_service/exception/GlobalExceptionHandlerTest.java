package com.pangeranvalerensco.orchestria.request_service.exception;

import com.pangeranvalerensco.orchestria.request_service.payload.response.ErrorResponse;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void optimisticLockConflictReturnsHttp409() {
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST",
                "/api/requests/4/settlement/approve");

        ResponseEntity<ErrorResponse<Void>> response =
                handler.handleOptimisticLockConflict(
                        new OptimisticLockException("conflict"),
                        request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(false, response.getBody().getSuccess());
        assertEquals(
                "Settlement sudah berubah atau telah diproses. Muat ulang data sebelum melanjutkan.",
                response.getBody().getMessage());
    }
}
