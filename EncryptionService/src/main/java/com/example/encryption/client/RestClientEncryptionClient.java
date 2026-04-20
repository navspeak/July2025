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
public class RestClientEncryptionClient implements FileEncryptionClient {

    private final RestClient restClient;

    public RestClientEncryptionClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8081/api/v1")
                .build();
    }

    @Override
    public Path encrypt(Path inputFile, String keyId, String algorithm) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(inputFile));

        if (keyId != null || algorithm != null) {
            builder.part("request", buildRequestJson(keyId, algorithm, inputFile.getFileName().toString()))
                    .contentType(MediaType.APPLICATION_JSON);
        }

        byte[] encrypted = restClient.post()
                .uri("/file/encrypt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
                .retrieve()
                .body(byte[].class);

        Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".enc");
        Files.write(outputFile, encrypted);
        return outputFile;
    }

    @Override
    public Path decrypt(Path encryptedFile, String keyId) throws IOException {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(encryptedFile));

        String uri = keyId != null ? "/file/decrypt?transitKey=" + keyId : "/file/decrypt";

        byte[] decrypted = restClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(builder.build())
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
