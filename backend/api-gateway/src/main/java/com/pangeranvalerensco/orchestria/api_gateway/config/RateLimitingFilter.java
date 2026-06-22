package com.pangeranvalerensco.orchestria.api_gateway.config;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    private static class TokenBucket {
        AtomicInteger tokens;
        long lastRefillTime;

        TokenBucket(int maxTokens) {
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = Instant.now().toEpochMilli();
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if ("POST".equalsIgnoreCase(method)) {
            if (path.equals("/api/auth/login")) {
                return applyRateLimit(exchange, chain, path, 5, 60000); // 5 req per min
            } else if (path.equals("/api/auth/otp/resend") || path.equals("/api/auth/password/forgot")) {
                return applyRateLimit(exchange, chain, path, 3, 60000); // 3 req per min
            }
        }

        return chain.filter(exchange);
    }

    private Mono<Void> applyRateLimit(ServerWebExchange exchange, WebFilterChain chain, String path, int maxTokens, long refillIntervalMillis) {
        String ip = getClientIp(exchange);
        String key = ip + ":" + path;

        TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(maxTokens));

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
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                String body = "{\"success\": false, \"message\": \"Terlalu banyak permintaan, silakan coba lagi nanti.\"}";
                return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes()))
                );
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
