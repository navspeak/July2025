package com.example.encryption.runner;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Primary
@Component
@Profile("runner | load | ping")
public class EncryptionServiceClient implements FileEncryptionClient {

    private final String baseUrl;
    private final TokenProvider tokenProvider;
    private final RestTemplate restTemplate;

    public EncryptionServiceClient(RunnerProperties props, TokenProvider tokenProvider) throws Exception {
        this.baseUrl = props.service().baseUrl();
        this.tokenProvider = tokenProvider;
        this.restTemplate = props.service().skipSslVerify() ? trustAllRestTemplate() : new RestTemplate();
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
            body.add("request", new HttpEntity<>(buildRequestJson(keyId, algorithm), partHeaders));
        }

        ResponseEntity<byte[]> response = exchange(baseUrl + "/file/encrypt", body);
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

        String uri = baseUrl + (keyId != null ? "/file/decrypt?transitKey=" + keyId : "/file/decrypt");
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
        headers.set(HttpHeaders.AUTHORIZATION, tokenProvider.bearerToken());
        return restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), byte[].class);
    }

    private String traceIdFrom(HttpHeaders headers) {
        String traceId = headers.getFirst("X-TraceId");
        return traceId != null ? traceId : "-";
    }

    private String buildRequestJson(String keyId, String algorithm) {
        return String.format("""
                {"transitKey":"%s","algorithm":"%s"}
                """,
                keyId != null ? keyId : "my-key",
                algorithm != null ? algorithm : "AES_256_GCM");
    }

    // Trusts all certs — only for local dev with self-signed certificates.
    private static RestTemplate trustAllRestTemplate() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] c, String a) {}
            public void checkServerTrusted(X509Certificate[] c, String a) {}
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((host, session) -> true);
        return new RestTemplate();
    }
}
