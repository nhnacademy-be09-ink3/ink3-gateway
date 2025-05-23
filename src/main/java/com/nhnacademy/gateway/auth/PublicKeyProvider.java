package com.nhnacademy.gateway.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhnacademy.gateway.exception.PublicKeyLoadException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class PublicKeyProvider {
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    private final AtomicReference<PublicKey> cachedKey = new AtomicReference<>();

    @Scheduled(fixedDelay = 3600000)
    public void fetchPublicKey() {
        try {
            log.info("🔐 Sending request to fetch the public key from remote server.");
            String response = webClientBuilder.build()
                    .get()
                    .uri("http://auth-service/auth/public-key")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(3));

            JsonNode root = objectMapper.readTree(response);
            String key = root.path("data").path("publicKey").asText();

            if (key.isBlank()) {
                throw new PublicKeyLoadException("Public key is empty");
            }

            key = key.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            cachedKey.set(KeyFactory.getInstance("RSA").generatePublic(keySpec));
            log.info("✅ Public key successfully updated.");
        } catch (Exception e) {
            log.error("❌ Failed to load public key.", e);
        }
    }

    public PublicKey get() {
        PublicKey key = cachedKey.get();
        if (key == null) {
            throw new IllegalStateException("Public key has not been initialized.");
        }
        return key;
    }
}
