package com.example.encryption.controller;

import com.example.encryption.vault.VaultTransitService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/text")
public class TextEncryptionController {

    private final VaultTransitService vaultTransitService;

    public TextEncryptionController(VaultTransitService vaultTransitService) {
        this.vaultTransitService = vaultTransitService;
    }

    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encrypt(
            @RequestBody Map<String, String> request) {

        String plaintext = request.get("plaintext");
        String keyId = request.get("keyId");

        String ciphertext = vaultTransitService.encryptText(plaintext, keyId);
        return ResponseEntity.ok(Map.of("ciphertext", ciphertext));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decrypt(
            @RequestBody Map<String, String> request) {

        String ciphertext = request.get("ciphertext");
        String keyId = request.get("keyId");

        String plaintext = vaultTransitService.decryptText(ciphertext, keyId);
        return ResponseEntity.ok(Map.of("plaintext", plaintext));
    }
}
