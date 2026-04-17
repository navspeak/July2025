package com.example.encryption.vault;

import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;

import java.util.Map;

@Service
public class VaultKVService {

    private final VaultTemplate vaultTemplate;

    public VaultKVService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void writeSecret(String path, Map<String, Object> data) {
        vaultTemplate.opsForVersionedKeyValue("secret").put(path, data);
    }

    public Map<String, Object> readSecret(String path) {
        Versioned<Map<String, Object>> response =
                vaultTemplate.opsForVersionedKeyValue("secret").get(path);
        return response != null ? response.getData() : Map.of();
    }

    public String readSecretValue(String path, String key) {
        Map<String, Object> data = readSecret(path);
        return data != null ? (String) data.get(key) : null;
    }

    public void deleteSecret(String path) {
        vaultTemplate.opsForVersionedKeyValue("secret").delete(path);
    }
}
