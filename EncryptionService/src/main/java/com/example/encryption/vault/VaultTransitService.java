package com.example.encryption.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class VaultTransitService {

    private final RestClient restClient;

    @Value("${vault.approle.role-id}")
    private String roleId;

    @Value("${vault.approle.secret-id}")
    private String secretId;

    @Value("${vault.transit-key:my-key}")
    private String defaultTransitKey;

    public VaultTransitService(@Value("${vault.uri:http://localhost:8200}") String vaultUri) {
        this.restClient = RestClient.builder()
                .baseUrl(vaultUri)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String wrapDek(String dekBase64, String keyName) {
        String token = authenticate();
        var response = post(token, "/v1/transit/encrypt/" + resolveKey(keyName),
                Map.of("plaintext", dekBase64));
        return (String) data(response).get("ciphertext");
    }

    public String unwrapDek(String wrappedDek, String keyName) {
        String token = authenticate();
        var response = post(token, "/v1/transit/decrypt/" + resolveKey(keyName),
                Map.of("ciphertext", wrappedDek));
        return (String) data(response).get("plaintext");
    }

    @SuppressWarnings("unchecked")
    private String authenticate() {
        var response = restClient.post()
                .uri("/v1/auth/approle/login")
                .body(Map.of("role_id", roleId, "secret_id", secretId))
                .retrieve()
                .body(Map.class);
        return (String) ((Map<String, Object>) response.get("auth")).get("client_token");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> post(String token, String path, Map<String, String> body) {
        return restClient.post()
                .uri(path)
                .header("X-Vault-Token", token)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(Map<String, Object> response) {
        return (Map<String, Object>) response.get("data");
    }

    private String resolveKey(String keyName) {
        return keyName != null ? keyName : defaultTransitKey;
    }
}
