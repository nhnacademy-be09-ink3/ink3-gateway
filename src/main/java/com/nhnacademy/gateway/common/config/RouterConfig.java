package com.nhnacademy.gateway.common.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("Ink3-API",
                p -> p.path("/api/hello")
                    .filters(f -> f.stripPrefix(1))
                    .uri("lb://INK3-API")
            )
            .build();

        //http://localhost:80/hello

        //        return builder.routes()
        //                .route("hello-service-a",
        //                        p->p.path("/hello").and().weight("hello",50).uri("http://localhost:8081/")
        //                        )
        //                .route("hello-service-b",
        //                        p->p.path("/hello").and().weight("hello",50).uri("http://localhost:8082/")
        //                        )
        //                .build();

        //        return builder.routes()
        //                .route("get_route", r -> r.path("/account")
        //                        .filters(o->o.addRequestHeader("uuid", UUID.randomUUID().toString()))
        //                        .uri("http://httpbin.org"))
        //                .build();

        //http://httpbin.org/get
    }
}
