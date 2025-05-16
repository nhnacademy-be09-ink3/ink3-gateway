package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.auth.JwtTokenValidator;
import io.jsonwebtoken.Claims;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    private final JwtTokenValidator jwtTokenValidator;

    List<String> WHITE_LIST = List.of(
            "/auth", "/shop"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        log.info("request: {}", request.getURI());

        if (WHITE_LIST.stream().anyMatch(request.getURI().getPath()::startsWith)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }

        String accessToken = authHeader.substring(7);

        try {
            Claims claims = jwtTokenValidator.validateAccessToken(accessToken);

            Long userId = claims.get("id", Long.class);
            String role = claims.get("role", String.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-ID", String.valueOf(userId))
                    .header("X-User-ROLE", role)
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Throwable throwable) {
            return onError(exchange, HttpStatus.UNAUTHORIZED);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }
}
