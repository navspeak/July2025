package com.example.encryption.controller;

import com.example.encryption.vault.VaultKVService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/secrets")
public class VaultKVController {

    private final VaultKVService vaultKVService;

    public VaultKVController(VaultKVService vaultKVService) {
        this.vaultKVService = vaultKVService;
    }

    @PostMapping("/{path}")
    public ResponseEntity<Void> write(
            @PathVariable String path,
            @RequestBody Map<String, Object> data) {

        vaultKVService.writeSecret(path, data);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{path}")
    public ResponseEntity<Map<String, Object>> read(@PathVariable String path) {
        Map<String, Object> data = vaultKVService.readSecret(path);
        return data.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(data);
    }

    @GetMapping("/{path}/{key}")
    public ResponseEntity<Map<String, String>> readKey(
            @PathVariable String path,
            @PathVariable String key) {

        String value = vaultKVService.readSecretValue(path, key);
        return value == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(Map.of(key, value));
    }

    @DeleteMapping("/{path}")
    public ResponseEntity<Void> delete(@PathVariable String path) {
        vaultKVService.deleteSecret(path);
        return ResponseEntity.noContent().build();
    }
}
