package com.nhnacademy.gateway.config;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties("gateway")
public class GatewayWhitelistProperties {
    private final List<String> whitelist;

    public GatewayWhitelistProperties(List<String> whitelist) {
        this.whitelist = Objects.isNull(whitelist) ? List.of() : whitelist;
    }
}
