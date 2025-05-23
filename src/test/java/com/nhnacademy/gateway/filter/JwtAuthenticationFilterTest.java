package com.nhnacademy.gateway.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nhnacademy.gateway.auth.JwtTokenValidator;
import com.nhnacademy.gateway.config.GatewayWhitelistProperties;
import com.nhnacademy.gateway.exception.TokenBlacklistedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {
    @Mock
    JwtTokenValidator jwtTokenValidator;

    GatewayWhitelistProperties properties;

    JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        properties = new GatewayWhitelistProperties(List.of("/whitelist/**"));
        filter = new JwtAuthenticationFilter(jwtTokenValidator, properties);
    }

    @Test
    void filterWithWhitelistedRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/whitelist/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        verify(chain).filter(any());
    }

    @Test
    void filterWithValidToken() {
        Claims claims = mock(Claims.class);
        when(claims.get("id", Long.class)).thenReturn(1L);
        when(claims.get("userType", String.class)).thenReturn("USER");
        when(claims.get("tokenType", String.class)).thenReturn("access");
        when(jwtTokenValidator.validateAccessToken("validToken")).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest.get("/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        ArgumentCaptor<ServerWebExchange> exchangeCaptor = ArgumentCaptor.forClass(ServerWebExchange.class);
        verify(chain).filter(exchangeCaptor.capture());

        ServerWebExchange mutatedExchange = exchangeCaptor.getValue();
        ServerHttpRequest mutatedRequest = mutatedExchange.getRequest();

        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Id")).isEqualTo("1");
        assertThat(mutatedRequest.getHeaders().getFirst("X-User-Role")).isEqualTo("USER");

        verify(chain).filter(any());
    }

    @Test
    void filterWithInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalidToken")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(jwtTokenValidator.validateAccessToken("invalidToken")).thenThrow(new JwtException("Invalid Token"));

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filterWithBlacklistedToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer blacklistedToken")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(jwtTokenValidator.validateAccessToken("blacklistedToken")).thenThrow(new TokenBlacklistedException());

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filterWithBlankToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(jwtTokenValidator.validateAccessToken("")).thenThrow(new IllegalArgumentException());

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void filterWithoutToken() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/protected")
                .build();

        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        Mono<Void> result = filter.filter(exchange, chain);
        result.block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }
}
