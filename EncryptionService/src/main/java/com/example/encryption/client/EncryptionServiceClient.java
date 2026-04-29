package com.example.encryption.client;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Primary
@Component
public class EncryptionServiceClient implements FileEncryptionClient {

    private static final String BASE_URL = "http://localhost:8081/api/v1";

    private final RestTemplate restTemplate;

    public EncryptionServiceClient() {
        this.restTemplate = new RestTemplate();
        // Never throw on non-2xx — caller inspects status via EncryptionResult
        this.restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override public boolean hasError(HttpStatusCode statusCode) { return false; }
        });
    }

    @Override
    public EncryptionResult encrypt(Path inputFile, String keyId, String algorithm) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(inputFile));

        if (keyId != null || algorithm != null) {
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.APPLICATION_JSON);
            body.add("request", new HttpEntity<>(buildRequestJson(keyId, algorithm, inputFile.getFileName().toString()), partHeaders));
        }

        ResponseEntity<byte[]> response = exchange(BASE_URL + "/file/encrypt", body);
        String traceId = traceIdFrom(response.getHeaders());
        int status = response.getStatusCode().value();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".enc");
            Files.write(outputFile, response.getBody());
            return new EncryptionResult(outputFile, traceId, status);
        }
        return new EncryptionResult(null, traceId, status);
    }

    @Override
    public EncryptionResult decrypt(Path encryptedFile, String keyId) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(encryptedFile));

        String uri = BASE_URL + (keyId != null ? "/file/decrypt?transitKey=" + keyId : "/file/decrypt");
        ResponseEntity<byte[]> response = exchange(uri, body);
        String traceId = traceIdFrom(response.getHeaders());
        int status = response.getStatusCode().value();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            String originalName = encryptedFile.getFileName().toString().replace(".enc", ".decrypted");
            Path outputFile = encryptedFile.resolveSibling(originalName);
            Files.write(outputFile, response.getBody());
            return new EncryptionResult(outputFile, traceId, status);
        }
        return new EncryptionResult(null, traceId, status);
    }

    private ResponseEntity<byte[]> exchange(String url, MultiValueMap<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), byte[].class);
    }

    private String traceIdFrom(HttpHeaders headers) {
        String traceId = headers.getFirst("X-TraceId");
        return traceId != null ? traceId : "-";
    }

    private String buildRequestJson(String keyId, String algorithm, String fileName) {
        return String.format("""
                {
                  "keyId": "%s",
                  "algorithm": "%s",
                  "fileName": "%s"
                }
                """,
                keyId != null ? keyId : "my-key",
                algorithm != null ? algorithm : "AES_256_GCM",
                fileName);
    }
}
