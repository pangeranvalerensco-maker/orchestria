package com.pangeranvalerensco.orchestria.api_gateway.config;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class RateLimitingFilter implements WebFilter, Ordered {

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.login.max-requests:5}")
    private int loginMax;
    @Value("${app.rate-limit.login.window-seconds:60}")
    private int loginWindow;

    @Value("${app.rate-limit.otp-verify.max-requests:10}")
    private int verifyMax;
    @Value("${app.rate-limit.otp-verify.window-seconds:60}")
    private int verifyWindow;

    @Value("${app.rate-limit.otp-resend.max-requests:3}")
    private int resendMax;
    @Value("${app.rate-limit.otp-resend.window-seconds:600}")
    private int resendWindow;

    @Value("${app.rate-limit.forgot-password.max-requests:3}")
    private int forgotMax;
    @Value("${app.rate-limit.forgot-password.window-seconds:600}")
    private int forgotWindow;

    @Value("${app.rate-limit.notification-email.max-requests:10}")
    private int emailMax;
    @Value("${app.rate-limit.notification-email.window-seconds:60}")
    private int emailWindow;

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private long lastCleanupTime = Instant.now().toEpochMilli();
    private static final int CLEANUP_THRESHOLD = 5000; // Lazy cleanup trigger

    private static class TokenBucket {
        AtomicInteger tokens;
        long lastRefillTime;
        long windowMillis;

        TokenBucket(int maxTokens, long windowMillis) {
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = Instant.now().toEpochMilli();
            this.windowMillis = windowMillis;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }

        lazyCleanup();

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if ("POST".equalsIgnoreCase(method)) {
            if (path.equals("/api/auth/login")) {
                return applyRateLimit(exchange, chain, path, loginMax, loginWindow * 1000L);
            } else if (path.equals("/api/auth/otp/verify")) {
                return applyRateLimit(exchange, chain, path, verifyMax, verifyWindow * 1000L);
            } else if (path.equals("/api/auth/otp/resend")) {
                return applyRateLimit(exchange, chain, path, resendMax, resendWindow * 1000L);
            } else if (path.equals("/api/auth/password/forgot")) {
                return applyRateLimit(exchange, chain, path, forgotMax, forgotWindow * 1000L);
            } else if (path.equals("/api/notifications/email")) {
                return applyRateLimit(exchange, chain, path, emailMax, emailWindow * 1000L);
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> applyRateLimit(ServerWebExchange exchange, WebFilterChain chain, String path, int maxTokens, long refillIntervalMillis) {
        String ip = getClientIp(exchange);
        String method = exchange.getRequest().getMethod().name();
        String key = ip + ":" + method + ":" + path;

        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxTokens, refillIntervalMillis));

        synchronized (bucket) {
            long now = Instant.now().toEpochMilli();
            long timePassed = now - bucket.lastRefillTime;

            if (timePassed > refillIntervalMillis) {
                bucket.tokens.set(maxTokens);
                bucket.lastRefillTime = now;
            }

            if (bucket.tokens.get() > 0) {
                bucket.tokens.decrementAndGet();
                return chain.filter(exchange);
            } else {
                long retryAfterSecs = (refillIntervalMillis - timePassed) / 1000;
                if (retryAfterSecs < 1) retryAfterSecs = 1;

                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                exchange.getResponse().getHeaders().add("Retry-After", String.valueOf(retryAfterSecs));
                
                String body = "{\"success\": false, \"message\": \"Terlalu banyak permintaan, silakan coba lagi nanti.\"}";
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                );
            }
        }
    }

    private void lazyCleanup() {
        if (buckets.size() > CLEANUP_THRESHOLD) {
            long now = Instant.now().toEpochMilli();
            if (now - lastCleanupTime > 60000) { // minimum 1 minute between cleanups
                lastCleanupTime = now;
                Iterator<Map.Entry<String, TokenBucket>> it = buckets.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, TokenBucket> entry = it.next();
                    TokenBucket bucket = entry.getValue();
                    synchronized (bucket) {
                        if (now - bucket.lastRefillTime > bucket.windowMillis) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    private String getClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = exchange.getRequest().getRemoteAddress() != null ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() : "Unknown";
        }
        return ip != null ? ip.split(",")[0].trim() : "Unknown";
    }

    @Override
    public int getOrder() {
        return -1; // run before security filter
    }
}
