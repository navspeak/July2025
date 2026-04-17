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
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody Map<String, String> request) {
        String ciphertext = vaultTransitService.encryptText(request.get("plaintext"), request.get("transitKey"));
        return ResponseEntity.ok(Map.of("ciphertext", ciphertext));
    }

    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decrypt(@RequestBody Map<String, String> request) {
        String plaintext = vaultTransitService.decryptText(request.get("ciphertext"), request.get("transitKey"));
        return ResponseEntity.ok(Map.of("plaintext", plaintext));
    }
}
