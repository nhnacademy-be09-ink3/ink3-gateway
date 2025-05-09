package com.nhnacademy.gateway.common.config;

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
            .route("shop-service",
                p -> p.path("/api")
                    .filters(f -> f.stripPrefix(1))
                    .uri("lb://SHOP-SERVICE")
            )
            .route("front-service",
                p -> p.path("/front")
                    .filters(f -> f.stripPrefix(1))
                    .uri("lb://FRONT-SERVICE")
            )
            .route("auth-service",
                p -> p.path("/auth")
                    .filters(f -> f.stripPrefix(1))
                    .uri("lb://AUTH-SERVICE")
            )
            .build();

        // 고정 서버 주소에 수동 분산
        // return builder.routes()
        //     .route("hello-service-a",
        //         p -> p.path("/hello").and().weight("hello", 50).uri("http://localhost:8081/")
        //     )
        //     .route("hello-service-b",
        //         p -> p.path("/hello").and().weight("hello", 50).uri("http://localhost:8082/")
        //     )
        //     .build();
    }
}
