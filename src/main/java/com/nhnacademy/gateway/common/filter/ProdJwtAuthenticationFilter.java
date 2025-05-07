package com.nhnacademy.gateway.common.filter;

import java.util.Objects;

import org.springframework.context.annotation.Profile;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Profile("dev")
@Component
public class ProdJwtAuthenticationFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String jwt = extractJwtFromCookie(exchange);

        // TODO: jwtProvider.validate(jwt) + userId 추출해서 아래에 반영
        if (jwt != null) {
            String fakeUserId = "parsed-user-id"; // 인증 서버 연동 전 임시 고정값
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-MEMBER-ID", fakeUserId)
                .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    private String extractJwtFromCookie(ServerWebExchange exchange) {
        return exchange.getRequest().getCookies().getFirst("jwt") != null
            ? Objects.requireNonNull(exchange.getRequest().getCookies().getFirst("jwt")).getValue()
            : null;
    }
}
