package com.example.encryption.controller;

import com.example.encryption.vault.VaultTransitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = ApiDocs.TAG_TEXT)
@RestController
@RequestMapping("/api/v1/text")
public class TextEncryptionController {

    private final VaultTransitService vaultTransitService;

    public TextEncryptionController(VaultTransitService vaultTransitService) {
        this.vaultTransitService = vaultTransitService;
    }

    @Operation(summary = ApiDocs.TEXT_ENCRYPT_SUMMARY)
    @ApiResponse(responseCode = "200", description = "Returns {ciphertext}")
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody Map<String, String> request) {
        String ciphertext = vaultTransitService.encryptText(request.get("plaintext"), request.get("transitKey"));
        return ResponseEntity.ok(Map.of("ciphertext", ciphertext));
    }

    @Operation(summary = ApiDocs.TEXT_DECRYPT_SUMMARY)
    @ApiResponse(responseCode = "200", description = "Returns {plaintext}")
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decrypt(@RequestBody Map<String, String> request) {
        String plaintext = vaultTransitService.decryptText(request.get("ciphertext"), request.get("transitKey"));
        return ResponseEntity.ok(Map.of("plaintext", plaintext));
    }
}
