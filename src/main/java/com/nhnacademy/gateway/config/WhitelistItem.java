package com.nhnacademy.gateway.config;

import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.pattern.PathPattern;

public record WhitelistItem(
        PathPattern pathPattern,
        List<HttpMethod> methods
) {
}
