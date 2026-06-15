package com.pangeranvalerensco.orchestria.api_gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class RequestLoggingFilter {

    @Bean
    public GlobalFilter logRequestFilter() {
        return (exchange, chain) -> {
            String method = exchange.getRequest().getMethod().name();
            String path = exchange.getRequest().getURI().getPath();

            log.info("Incoming request: {} {}", method, path);

            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        int statusCode = exchange.getResponse().getStatusCode() != null
                                ? exchange.getResponse().getStatusCode().value()
                                : 0;

                        log.info("Completed request: {} {} -> {}", method, path, statusCode);
                    }));
        };
    }
}
