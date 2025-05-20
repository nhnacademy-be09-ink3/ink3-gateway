package com.nhnacademy.gateway.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.nhnacademy.gateway.exception.TokenBlacklistedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Jwts.SIG;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class JwtTokenValidatorTest {
    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    PublicKeyProvider publicKeyProvider;

    @InjectMocks
    JwtTokenValidator jwtTokenValidator;

    @Test
    void validateAccessToken() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        when(publicKeyProvider.get()).thenReturn(publicKey);

        String token = Jwts.builder()
                .claim("id", 1L)
                .claim("role", "USER")
                .subject("username")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(privateKey, SIG.RS256)
                .compact();

        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(false);

        Claims claims = jwtTokenValidator.validateAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("username");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    @Test
    void validateBlacklistedAccessToken() {
        String token = "blacklistedToken";
        when(redisTemplate.hasKey("blacklist:" + token)).thenReturn(true);

        assertThrows(TokenBlacklistedException.class, () -> jwtTokenValidator.validateAccessToken(token));
    }
}
