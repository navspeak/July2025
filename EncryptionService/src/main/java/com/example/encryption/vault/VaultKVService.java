package com.example.encryption.vault;

import com.example.encryption.config.VaultProperties;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Versioned;

import java.util.Map;

@Service
public class VaultKVService {

    private final VaultTemplate vaultTemplate;
    private final VaultProperties vaultProperties;

    public VaultKVService(VaultTemplate vaultTemplate, VaultProperties vaultProperties) {
        this.vaultTemplate = vaultTemplate;
        this.vaultProperties = vaultProperties;
    }

    public void writeSecret(String path, Map<String, Object> data) {
        vaultTemplate.opsForVersionedKeyValue(vaultProperties.kvMountPath()).put(path, data);
    }

    public Map<String, Object> readSecret(String path) {
        Versioned<Map<String, Object>> response =
                vaultTemplate.opsForVersionedKeyValue(vaultProperties.kvMountPath()).get(path);
        return response != null ? response.getData() : Map.of();
    }

    public String readSecretValue(String path, String key) {
        Map<String, Object> data = readSecret(path);
        return data != null ? (String) data.get(key) : null;
    }

    public void deleteSecret(String path) {
        vaultTemplate.opsForVersionedKeyValue(vaultProperties.kvMountPath()).delete(path);
    }
}
