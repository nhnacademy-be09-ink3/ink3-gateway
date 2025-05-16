package com.nhnacademy.gateway.auth;

import com.nhnacademy.gateway.exception.TokenBlacklistedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class JwtTokenValidator {
    private final PublicKeyProvider publicKeyProvider;
    private final RedisTemplate<String, String> redisTemplate;

    public Claims validateAccessToken(String accessToken) {
        if (redisTemplate.hasKey("blacklist:" + accessToken)) {
            throw new TokenBlacklistedException();
        }

        return Jwts.parser()
                .verifyWith(publicKeyProvider.get())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }
}
