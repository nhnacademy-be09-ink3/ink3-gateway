package com.nhnacademy.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // Eureka 등록 기반 자동 분산
        return builder.routes()
                .route("auth-ping",
                        p -> p.path("/auth/ping")
                                .uri("no://ECHO")
                )
                .route("auth-service",
                        p -> p.path("/auth/**")
                                .uri("lb://AUTH-SERVICE")
                )
                .route("shop-service",
                        p -> p.path("/shop/**")
                                .uri("lb://SHOP-SERVICE")
                )
                .build();
    }
}
