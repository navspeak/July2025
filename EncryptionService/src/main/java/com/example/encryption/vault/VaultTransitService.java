package com.example.encryption.vault;

import com.example.encryption.config.VaultProperties;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Ciphertext;
import org.springframework.vault.support.Plaintext;

@Service
public class VaultTransitService {

    private final VaultTemplate vaultTemplate;
    private final VaultProperties vaultProperties;

    public VaultTransitService(VaultTemplate vaultTemplate, VaultProperties vaultProperties) {
        this.vaultTemplate = vaultTemplate;
        this.vaultProperties = vaultProperties;
    }

    public String wrapDek(String dekBase64, String keyName) {
        return vaultTemplate.opsForTransit(vaultProperties.mountPath())
                .encrypt(resolveKey(keyName), Plaintext.of(dekBase64))
                .getCiphertext();
    }

    public String unwrapDek(String wrappedDek, String keyName) {
        return vaultTemplate.opsForTransit(vaultProperties.mountPath())
                .decrypt(resolveKey(keyName), Ciphertext.of(wrappedDek))
                .asString();
    }

    public String encryptText(String plaintext, String keyName) {
        return vaultTemplate.opsForTransit(vaultProperties.mountPath())
                .encrypt(resolveKey(keyName), Plaintext.of(plaintext))
                .getCiphertext();
    }

    public String decryptText(String ciphertext, String keyName) {
        return vaultTemplate.opsForTransit(vaultProperties.mountPath())
                .decrypt(resolveKey(keyName), Ciphertext.of(ciphertext))
                .asString();
    }

    private String resolveKey(String keyName) {
        return keyName != null ? keyName : vaultProperties.keyName();
    }
}
