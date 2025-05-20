package com.nhnacademy.gateway.auth;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class PublicKeyProviderTest {
    @Mock
    ObjectMapper objectMapper;

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    WebClient webClient;

    @InjectMocks
    PublicKeyProvider publicKeyProvider;

    @Test
    void fetchPublicKey() throws Exception {
        String pem = """
                -----BEGIN PUBLIC KEY-----
                MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyKrbRQu4og6d95aTS1Rd
                EBCkaZXgi4FCbv4gW4mRO/1Xr52vWRKddAEWegLL1BliG+Hu360pLbhDK3+sKHEk
                FOtscyQ5iS6ltkKdQ4ewMH6f3Xeq8hJm3AWRx2bn4a/4O7kq7NPUXlow1Rn/Fodp
                xx/+brklPpNK2neIpDICLz+GWd9wE8dx07iK00iP/k9sYtoRIaOu2dsMBksg8W5/
                ENfPv4dxNoUEOJ0Mtvl+/0VafryaHBFe3ujN5BFeE5drvuoTDju1CgcK3/1Bi1T0
                ncuwE0nQwLTyKy+pFzMF2mE2nRJPI6ThkRvy9q1RgxjihKNBlPRIIc+qgttwefaO
                zwIDAQAB
                -----END PUBLIC KEY-----
                """;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        ObjectNode data = root.putObject("data");
        data.put("publicKey", pem);
        String responseJson = mapper.writeValueAsString(root);

        JsonNode mockNode = mapper.readTree(responseJson);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()
                .uri("http://auth-service/auth/public-key")
                .retrieve()
                .bodyToMono(String.class))
                .thenReturn(Mono.just(responseJson));

        when(objectMapper.readTree(responseJson)).thenReturn(mockNode);

        publicKeyProvider.fetchPublicKey();

        assertNotNull(publicKeyProvider.get());
    }

    @Test
    void fetchPublicKeyWithFail() throws JsonProcessingException {
        String invalidPem = "-----BEGIN PUBLIC KEY-----\nTHIS_IS_NOT_BASE64!!@@\n-----END PUBLIC KEY-----";

        ObjectNode root = new ObjectMapper().createObjectNode();
        ObjectNode data = root.putObject("data");
        data.put("publicKey", invalidPem);
        String responseJson = new ObjectMapper().writeValueAsString(root);
        JsonNode mockNode = new ObjectMapper().readTree(responseJson);

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()
                .uri("http://auth-service/auth/public-key")
                .retrieve()
                .bodyToMono(String.class))
                .thenReturn(Mono.just(responseJson));

        when(objectMapper.readTree(responseJson)).thenReturn(mockNode);

        assertDoesNotThrow(() -> publicKeyProvider.fetchPublicKey());

        assertThrows(IllegalStateException.class, () -> publicKeyProvider.get());
    }

    @Test
    void getPublicKey_beforeInit_throwsException() {
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> publicKeyProvider.get());
        assertEquals("Public key has not been initialized.", e.getMessage());
    }
}
