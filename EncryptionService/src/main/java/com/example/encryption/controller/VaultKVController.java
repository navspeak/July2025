package com.example.encryption.controller;

import com.example.encryption.vault.VaultKVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = ApiDocs.TAG_SECRETS)
@Profile("!client")
@RestController
@RequestMapping("/api/v1/secrets")
public class VaultKVController {

    private final VaultKVService vaultKVService;

    public VaultKVController(VaultKVService vaultKVService) {
        this.vaultKVService = vaultKVService;
    }

    @Operation(summary = ApiDocs.SECRET_WRITE_SUMMARY)
    @ApiResponse(responseCode = "204", description = ApiDocs.RESP_204)
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping(value = "/{path}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> write(
            @PathVariable String path,
            @RequestBody Map<String, Object> data) {

        vaultKVService.writeSecret(path, data);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = ApiDocs.SECRET_READ_SUMMARY)
    @ApiResponse(responseCode = "200", description = "Key-value map at path",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "404", description = ApiDocs.RESP_404)
    @GetMapping(value = "/{path}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> read(@PathVariable String path) {
        Map<String, Object> data = vaultKVService.readSecret(path);
        return data.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(data);
    }

    @Operation(summary = ApiDocs.SECRET_READ_KEY_SUMMARY)
    @ApiResponse(responseCode = "200", description = "Single key-value entry",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "404", description = ApiDocs.RESP_404)
    @GetMapping(value = "/{path}/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> readKey(
            @PathVariable String path,
            @PathVariable String key) {

        String value = vaultKVService.readSecretValue(path, key);
        return value == null
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(Map.of(key, value));
    }

    @Operation(summary = ApiDocs.SECRET_DELETE_SUMMARY)
    @ApiResponse(responseCode = "204", description = ApiDocs.RESP_204)
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @DeleteMapping("/{path}")
    public ResponseEntity<Void> delete(@PathVariable String path) {
        vaultKVService.deleteSecret(path);
        return ResponseEntity.noContent().build();
    }
}
