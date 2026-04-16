package com.example.encryption.client;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class EncryptionServiceClient {

    private final RestClient restClient;

    public EncryptionServiceClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081/api/v1")
                .build();
    }

    public Path encrypt(Path inputFile, String keyId, String algorithm) throws IOException {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", new FileSystemResource(inputFile));

        if (keyId != null || algorithm != null) {
            String request = buildRequestJson(keyId, algorithm, inputFile.getFileName().toString());
            body.part("request", request, MediaType.APPLICATION_JSON);
        }

        byte[] encrypted = restClient.post()
                .uri("/encrypt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(byte[].class);

        Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".enc");
        Files.write(outputFile, encrypted);
        return outputFile;
    }

    public Path decrypt(Path encryptedFile, String keyId) throws IOException {
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("file", new FileSystemResource(encryptedFile));

        String uri = keyId != null ? "/decrypt?keyId=" + keyId : "/decrypt";

        byte[] decrypted = restClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(byte[].class);

        String originalName = encryptedFile.getFileName().toString().replace(".enc", ".decrypted");
        Path outputFile = encryptedFile.resolveSibling(originalName);
        Files.write(outputFile, decrypted);
        return outputFile;
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
