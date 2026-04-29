package com.example.encryption.client;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class WebClientEncryptionClient implements FileEncryptionClient {

    private final WebClient webClient;

    public WebClientEncryptionClient() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:8081/api/v1")
                .build();
    }

    @Override
    public EncryptionResult encrypt(Path inputFile, String keyId, String algorithm) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(inputFile));

        if (keyId != null || algorithm != null) {
            builder.part("request", buildRequestJson(keyId, algorithm, inputFile.getFileName().toString()))
                    .contentType(MediaType.APPLICATION_JSON);
        }

        ResponseEntity<byte[]> response = webClient.post()
                .uri("/file/encrypt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        String traceId = traceIdFrom(response);
        Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".enc");
        Files.write(outputFile, response.getBody());
        return new EncryptionResult(outputFile, traceId, response.getStatusCode().value());
    }

    @Override
    public EncryptionResult decrypt(Path encryptedFile, String keyId) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(encryptedFile));

        String uri = keyId != null ? "/file/decrypt?transitKey=" + keyId : "/file/decrypt";

        ResponseEntity<byte[]> response = webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .retrieve()
                .toEntity(byte[].class)
                .block();

        String traceId = traceIdFrom(response);
        String originalName = encryptedFile.getFileName().toString().replace(".enc", ".decrypted");
        Path outputFile = encryptedFile.resolveSibling(originalName);
        Files.write(outputFile, response.getBody());
        return new EncryptionResult(outputFile, traceId, response.getStatusCode().value());
    }

    private String traceIdFrom(ResponseEntity<?> response) {
        String traceId = response.getHeaders().getFirst("X-TraceId");
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
