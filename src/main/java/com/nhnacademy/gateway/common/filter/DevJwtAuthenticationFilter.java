package com.nhnacademy.gateway.common.filter;

import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Profile("dev")
@Component
public class DevJwtAuthenticationFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header("X-MEMBER-ID", "admin")
            .build();
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }
}
