package com.example.encryption.controller;

import com.example.encryption.vault.VaultTransitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/text")
@Tag(name = "Text Encryption", description = "Encrypt and decrypt text values via Vault transit engine")
public class TextEncryptionController {

    private final VaultTransitService vaultTransitService;

    public TextEncryptionController(VaultTransitService vaultTransitService) {
        this.vaultTransitService = vaultTransitService;
    }

    @Operation(
        summary = "Encrypt a text value",
        description = "Encrypts a plaintext string using Vault transit. Useful for individual field values such as PAN or SSN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(value = """
                    {"plaintext": "4111111111111111", "keyId": "my-key"}
                    """))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Vault ciphertext token",
                content = @Content(examples = @ExampleObject(value = """
                    {"ciphertext": "vault:v1:ABCxyz..."}
                    """)))
        }
    )
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody Map<String, String> request) {
        String ciphertext = vaultTransitService.encryptText(request.get("plaintext"), request.get("keyId"));
        return ResponseEntity.ok(Map.of("ciphertext", ciphertext));
    }

    @Operation(
        summary = "Decrypt a text value",
        description = "Decrypts a Vault ciphertext token back to its original plaintext value.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(value = """
                    {"ciphertext": "vault:v1:ABCxyz...", "keyId": "my-key"}
                    """))
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Decrypted plaintext",
                content = @Content(examples = @ExampleObject(value = """
                    {"plaintext": "4111111111111111"}
                    """)))
        }
    )
    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, String>> decrypt(@RequestBody Map<String, String> request) {
        String plaintext = vaultTransitService.decryptText(request.get("ciphertext"), request.get("keyId"));
        return ResponseEntity.ok(Map.of("plaintext", plaintext));
    }
}
