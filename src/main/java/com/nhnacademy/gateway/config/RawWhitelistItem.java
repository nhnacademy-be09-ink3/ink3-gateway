package com.nhnacademy.gateway.config;

import java.util.List;

public record RawWhitelistItem(
        String path,
        List<String> methods
) {
}
