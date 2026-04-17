package com.example.encryption.vault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.Plaintext;
import org.springframework.vault.support.Ciphertext;

@Service
public class VaultTransitService {

    private final VaultTemplate vaultTemplate;

    @Value("${vault.transit-key:my-key}")
    private String defaultTransitKey;

    public VaultTransitService(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public String wrapDek(String dekBase64, String keyName) {
        return vaultTemplate.opsForTransit()
                .encrypt(resolveKey(keyName), Plaintext.of(dekBase64))
                .getCiphertext();
    }

    public String unwrapDek(String wrappedDek, String keyName) {
        return vaultTemplate.opsForTransit()
                .decrypt(resolveKey(keyName), Ciphertext.of(wrappedDek))
                .asString();
    }

    public String encryptText(String plaintext, String keyName) {
        return vaultTemplate.opsForTransit()
                .encrypt(resolveKey(keyName), Plaintext.of(plaintext))
                .getCiphertext();
    }

    public String decryptText(String ciphertext, String keyName) {
        return vaultTemplate.opsForTransit()
                .decrypt(resolveKey(keyName), Ciphertext.of(ciphertext))
                .asString();
    }

    private String resolveKey(String keyName) {
        return keyName != null ? keyName : defaultTransitKey;
    }
}
