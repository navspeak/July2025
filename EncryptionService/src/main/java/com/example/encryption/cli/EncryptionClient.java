package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.function.Supplier;

@Slf4j
@Component
@Profile("cli")
public class EncryptionClient {

    private final RestClient restClient;
    private final TokenProvider tokenProvider;

    public EncryptionClient(RestClient restClient, TokenProvider tokenProvider) {
        this.restClient = restClient;
        this.tokenProvider = tokenProvider;
    }

    public byte[] encryptFile(byte[] fileBytes, String fileName,
                              EncryptionAlgorithm algorithm, String transitKey) {
        return withRetry(() -> {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", namedResource(fileBytes, fileName));
            if (algorithm != null || transitKey != null) {
                body.add("request", new FileEncryptRequest(algorithm, transitKey));
            }
            return restClient.post()
                    .uri("/api/v1/file/encrypt")
                    .header("Authorization", "Bearer " + tokenProvider.getToken())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(byte[].class);
        });
    }

    public String encryptText(String plaintext, String transitKey) {
        return withRetry(() -> {
            TextEncryptResponse response = restClient.post()
                    .uri("/api/v1/text/encrypt")
                    .header("Authorization", "Bearer " + tokenProvider.getToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TextEncryptRequest(plaintext, transitKey))
                    .retrieve()
                    .body(TextEncryptResponse.class);
            return response == null ? null : response.ciphertext();
        });
    }

    private <T> T withRetry(Supplier<T> call) {
        try {
            return call.get();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.debug("401 received — refreshing token and retrying");
                tokenProvider.invalidate();
                return call.get();
            }
            throw e;
        }
    }

    private ByteArrayResource namedResource(byte[] bytes, String fileName) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() { return fileName; }
        };
    }

    private record FileEncryptRequest(
            EncryptionAlgorithm algorithm,
            String transitKey) {}

    private record TextEncryptRequest(
            String plaintext,
            String transitKey) {}

    private record TextEncryptResponse(
            @JsonProperty("ciphertext") String ciphertext) {}
}
