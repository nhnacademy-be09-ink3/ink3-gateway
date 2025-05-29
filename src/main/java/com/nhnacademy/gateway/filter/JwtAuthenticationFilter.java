package com.nhnacademy.gateway.filter;

import com.nhnacademy.gateway.auth.JwtTokenValidator;
import com.nhnacademy.gateway.config.GatewayWhitelistCache;
import com.nhnacademy.gateway.exception.TokenBlacklistedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter implements GlobalFilter {
    private final JwtTokenValidator jwtTokenValidator;
    private final GatewayWhitelistCache gatewayWhitelistCache;

    private boolean isWhitelisted(String path) {
        PathContainer container = PathContainer.parsePath(path);
        return gatewayWhitelistCache.getWhiteListPatterns().stream()
                .anyMatch(pattern -> pattern.matches(container));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isWhitelisted(request.getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (Objects.isNull(authHeader) || !authHeader.startsWith("Bearer ")) {
            return onError(exchange);
        }

        String accessToken = authHeader.substring(7);

        try {
            Claims claims = jwtTokenValidator.validateAccessToken(accessToken);

            if (!claims.get("tokenType", String.class).equals("access")) {
                return onError(exchange);
            }

            Long userId = claims.get("id", Long.class);
            String userType = claims.get("userType", String.class);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", String.valueOf(userId))
                    .header("X-User-Role", userType)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException | IllegalArgumentException | TokenBlacklistedException e) {
            return onError(exchange);
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        log.warn("Unauthorized access attempt: {}", exchange.getRequest().getURI());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
