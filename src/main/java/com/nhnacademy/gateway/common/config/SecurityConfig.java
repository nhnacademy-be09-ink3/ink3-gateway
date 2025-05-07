package com.nhnacademy.gateway.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import com.nhnacademy.gateway.common.filter.DevJwtAuthenticationFilter;
import com.nhnacademy.gateway.common.filter.ProdJwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final DevJwtAuthenticationFilter devFilter;
    private final ProdJwtAuthenticationFilter prodFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/**").permitAll()
            )
            .addFilterAt(devFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .addFilterAt(prodFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
}
