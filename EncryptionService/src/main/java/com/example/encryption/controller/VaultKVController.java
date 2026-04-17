package com.example.encryption.controller;

import com.example.encryption.vault.VaultKVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/secrets")
@Tag(name = "Vault KV", description = "Read and write secrets in Vault KV v2")
public class VaultKVController {

    private final VaultKVService vaultKVService;

    public VaultKVController(VaultKVService vaultKVService) {
        this.vaultKVService = vaultKVService;
    }

    @Operation(summary = "Write a secret", description = "Stores key-value pairs at the given Vault KV path.",
        responses = @ApiResponse(responseCode = "204", description = "Secret stored"))
    @PostMapping("/{path}")
    public ResponseEntity<Void> write(
            @Parameter(description = "Secret path e.g. myapp/config") @PathVariable String path,
            @RequestBody Map<String, Object> data) {

        vaultKVService.writeSecret(path, data);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Read all keys at a path",
        responses = {
            @ApiResponse(responseCode = "200", description = "Secret data"),
            @ApiResponse(responseCode = "404", description = "Path not found")
        })
    @GetMapping("/{path}")
    public ResponseEntity<Map<String, Object>> read(
            @Parameter(description = "Secret path e.g. myapp/config") @PathVariable String path) {

        Map<String, Object> data = vaultKVService.readSecret(path);
        return data.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(data);
    }

    @Operation(summary = "Read a single key from a path",
        responses = {
            @ApiResponse(responseCode = "200", description = "Key value"),
            @ApiResponse(responseCode = "404", description = "Path or key not found")
        })
    @GetMapping("/{path}/{key}")
    public ResponseEntity<Map<String, String>> readKey(
            @Parameter(description = "Secret path") @PathVariable String path,
            @Parameter(description = "Key name") @PathVariable String key) {

        String value = vaultKVService.readSecretValue(path, key);
        return value == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(Map.of(key, value));
    }

    @Operation(summary = "Delete a secret", description = "Soft-deletes the latest version at the given path.",
        responses = @ApiResponse(responseCode = "204", description = "Secret deleted"))
    @DeleteMapping("/{path}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Secret path") @PathVariable String path) {

        vaultKVService.deleteSecret(path);
        return ResponseEntity.noContent().build();
    }
}
