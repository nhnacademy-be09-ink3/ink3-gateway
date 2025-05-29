package com.nhnacademy.gateway.config;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

@Slf4j
@Component
public class GatewayWhitelistCache {
    private final Environment environment;
    private final PathPatternParser parser = new PathPatternParser();
    @Getter
    private volatile List<PathPattern> whiteListPatterns = List.of();

    public GatewayWhitelistCache(Environment environment) {
        this.environment = environment;
        refreshPatterns();
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        log.info("🌀 Config Refresh Event Triggered - Reloading Whitelist...");
        refreshPatterns();
    }

    private void refreshPatterns() {
        String[] whitelist = environment.getProperty("gateway.whitelist", String[].class, new String[0]);
        this.whiteListPatterns = Arrays.stream(whitelist)
                .map(parser::parse)
                .toList();
        log.info("✅ Whitelist Updated: {}", whiteListPatterns);
    }
}
