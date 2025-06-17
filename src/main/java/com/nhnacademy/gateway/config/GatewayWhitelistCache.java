package com.nhnacademy.gateway.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPatternParser;

@Slf4j
@Component
public class GatewayWhitelistCache {
    private final Environment environment;
    private final PathPatternParser parser = new PathPatternParser();
    private final AtomicReference<List<WhitelistItem>> whitelist = new AtomicReference<>(List.of());

    public GatewayWhitelistCache(Environment environment) {
        this.environment = environment;
        refreshPatterns();
    }

    public List<WhitelistItem> getWhitelist() {
        return whitelist.get();
    }

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh() {
        log.info("🌀 Config Refresh Event Triggered - Reloading Whitelist...");
        refreshPatterns();
    }

    private void refreshPatterns() {
        List<RawWhitelistItem> rawList = Binder.get(environment)
                .bind("gateway.whitelist", Bindable.listOf(RawWhitelistItem.class))
                .orElse(List.of());
        this.whitelist.set(rawList.stream()
                .map(item -> new WhitelistItem(
                        parser.parse(item.path()),
                        item.methods().stream().map(HttpMethod::valueOf).toList())
                ).toList());
        log.info("📃 Whitelist Updated: {}", whitelist.get());
    }
}
