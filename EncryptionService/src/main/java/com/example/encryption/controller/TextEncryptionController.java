package com.example.encryption.controller;

import com.example.encryption.dto.TextDecryptRequest;
import com.example.encryption.dto.TextEncryptRequest;
import com.example.encryption.vault.VaultTransitService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
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
    @ApiResponse(responseCode = "200", description = "Returns {ciphertext}",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping(value = "/encrypt",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> encrypt(@RequestBody TextEncryptRequest request) {
        String ciphertext = vaultTransitService.encryptText(request.plaintext(), request.transitKey());
        return ResponseEntity.ok(Map.of("ciphertext", ciphertext));
    }

    @Operation(summary = ApiDocs.TEXT_DECRYPT_SUMMARY)
    @ApiResponse(responseCode = "200", description = "Returns {plaintext}",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping(value = "/decrypt",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> decrypt(@RequestBody TextDecryptRequest request) {
        String plaintext = vaultTransitService.decryptText(request.ciphertext(), request.transitKey());
        return ResponseEntity.ok(Map.of("plaintext", plaintext));
    }
}
