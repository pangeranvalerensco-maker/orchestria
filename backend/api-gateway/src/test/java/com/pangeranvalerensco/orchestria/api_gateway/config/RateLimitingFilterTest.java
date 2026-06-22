package com.pangeranvalerensco.orchestria.api_gateway.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;

public class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new RateLimitingFilter();
        ReflectionTestUtils.setField(filter, "enabled", true);
        ReflectionTestUtils.setField(filter, "loginMax", 2);
        ReflectionTestUtils.setField(filter, "loginWindow", 1);
        ReflectionTestUtils.setField(filter, "verifyMax", 2);
        ReflectionTestUtils.setField(filter, "verifyWindow", 1);
        ReflectionTestUtils.setField(filter, "emailMax", 2);
        ReflectionTestUtils.setField(filter, "emailWindow", 1);
    }

    private WebFilterChain mockChain() {
        return exchange -> Mono.empty();
    }

    private MockServerWebExchange createExchange(String path, String ip) {
        MockServerHttpRequest request = MockServerHttpRequest.method(HttpMethod.POST, path)
                .header("X-Forwarded-For", ip)
                .build();
        return MockServerWebExchange.from(request);
    }

    @Test
    public void testUnderLimit() {
        MockServerWebExchange exchange = createExchange("/api/auth/login", "192.168.1.1");
        
        filter.filter(exchange, mockChain()).block();
        assertNull(exchange.getResponse().getStatusCode()); // Not modified, proceeds to chain
    }

    @Test
    public void testOverLimitReturns429() {
        MockServerWebExchange ex1 = createExchange("/api/auth/login", "192.168.1.2");
        MockServerWebExchange ex2 = createExchange("/api/auth/login", "192.168.1.2");
        MockServerWebExchange ex3 = createExchange("/api/auth/login", "192.168.1.2");

        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        filter.filter(ex3, mockChain()).block();

        assertNull(ex1.getResponse().getStatusCode());
        assertNull(ex2.getResponse().getStatusCode());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex3.getResponse().getStatusCode());
        assertNotNull(ex3.getResponse().getHeaders().getFirst("Retry-After"));
    }

    @Test
    public void testDifferentIpsHaveDifferentBuckets() {
        MockServerWebExchange ex1 = createExchange("/api/auth/login", "10.0.0.1");
        MockServerWebExchange ex2 = createExchange("/api/auth/login", "10.0.0.1");
        MockServerWebExchange ex3 = createExchange("/api/auth/login", "10.0.0.2"); // Different IP

        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        filter.filter(ex3, mockChain()).block(); // Should pass

        assertNull(ex3.getResponse().getStatusCode());
    }

    @Test
    public void testNonSensitiveEndpoint() {
        MockServerWebExchange ex1 = createExchange("/api/auth/register", "127.0.0.1");
        MockServerWebExchange ex2 = createExchange("/api/auth/register", "127.0.0.1");
        MockServerWebExchange ex3 = createExchange("/api/auth/register", "127.0.0.1");

        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        filter.filter(ex3, mockChain()).block();

        assertNull(ex3.getResponse().getStatusCode());
    }

    @Test
    public void testDisabledLimiter() {
        ReflectionTestUtils.setField(filter, "enabled", false);
        
        MockServerWebExchange ex1 = createExchange("/api/auth/login", "127.0.0.2");
        MockServerWebExchange ex2 = createExchange("/api/auth/login", "127.0.0.2");
        MockServerWebExchange ex3 = createExchange("/api/auth/login", "127.0.0.2");

        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        filter.filter(ex3, mockChain()).block();

        assertNull(ex3.getResponse().getStatusCode()); // Limit disabled
    }

    @Test
    public void testWindowExpiry() throws InterruptedException {
        MockServerWebExchange ex1 = createExchange("/api/auth/login", "127.0.0.3");
        MockServerWebExchange ex2 = createExchange("/api/auth/login", "127.0.0.3");
        
        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        
        Thread.sleep(1100); // Wait for 1 second window to expire
        
        MockServerWebExchange ex3 = createExchange("/api/auth/login", "127.0.0.3");
        filter.filter(ex3, mockChain()).block();
        
        assertNull(ex3.getResponse().getStatusCode());
    }

    @Test
    public void testOtpVerifyAndNotificationEmail() {
        MockServerWebExchange ex1 = createExchange("/api/auth/otp/verify", "8.8.8.8");
        MockServerWebExchange ex2 = createExchange("/api/auth/otp/verify", "8.8.8.8");
        MockServerWebExchange ex3 = createExchange("/api/auth/otp/verify", "8.8.8.8");

        filter.filter(ex1, mockChain()).block();
        filter.filter(ex2, mockChain()).block();
        filter.filter(ex3, mockChain()).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex3.getResponse().getStatusCode());
        
        MockServerWebExchange exE1 = createExchange("/api/notifications/email", "8.8.4.4");
        MockServerWebExchange exE2 = createExchange("/api/notifications/email", "8.8.4.4");
        MockServerWebExchange exE3 = createExchange("/api/notifications/email", "8.8.4.4");

        filter.filter(exE1, mockChain()).block();
        filter.filter(exE2, mockChain()).block();
        filter.filter(exE3, mockChain()).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exE3.getResponse().getStatusCode());
    }
}
