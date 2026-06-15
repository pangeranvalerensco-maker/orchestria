package com.pangeranvalerensco.orchestria.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", route -> route
                        .path("/api/auth/**")
                        .uri("http://localhost:8001"))

                .route("organization-service", route -> route
                        .path("/api/organization/**")
                        .uri("http://localhost:8002"))

                .route("request-service", route -> route
                        .path("/api/requests/**")
                        .uri("http://localhost:8003"))

                .route("finance-service", route -> route
                        .path("/api/finance/**")
                        .uri("http://localhost:8004"))

                .build();
    }
}