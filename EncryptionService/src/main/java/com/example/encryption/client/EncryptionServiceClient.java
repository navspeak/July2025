package com.example.encryption.client;

import org.springframework.context.annotation.Primary;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
    }

    @Override
    public Path encrypt(Path inputFile, String keyId, String algorithm) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(inputFile));

        if (keyId != null || algorithm != null) {
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.APPLICATION_JSON);
            body.add("request", new HttpEntity<>(buildRequestJson(keyId, algorithm, inputFile.getFileName().toString()), partHeaders));
        }

        byte[] encrypted = post(BASE_URL + "/file/encrypt", body, byte[].class);
        Path outputFile = inputFile.resolveSibling(inputFile.getFileName() + ".enc");
        Files.write(outputFile, encrypted);
        return outputFile;
    }

    @Override
    public Path decrypt(Path encryptedFile, String keyId) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(encryptedFile));

        String uri = BASE_URL + (keyId != null ? "/file/decrypt?transitKey=" + keyId : "/file/decrypt");
        byte[] decrypted = post(uri, body, byte[].class);

        String originalName = encryptedFile.getFileName().toString().replace(".enc", ".decrypted");
        Path outputFile = encryptedFile.resolveSibling(originalName);
        Files.write(outputFile, decrypted);
        return outputFile;
    }

    private <T> T post(String url, MultiValueMap<String, Object> body, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return restTemplate.postForObject(url, new HttpEntity<>(body, headers), responseType);
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
