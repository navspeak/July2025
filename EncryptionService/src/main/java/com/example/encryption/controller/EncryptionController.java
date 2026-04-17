package com.example.encryption.controller;

import com.example.encryption.dto.EncryptionRequest;
import com.example.encryption.service.FileEncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "File Encryption", description = "Encrypt and decrypt files using AES-256-GCM or ChaCha20-Poly1305")
public class EncryptionController {

    private final FileEncryptionService fileEncryptionService;

    public EncryptionController(FileEncryptionService fileEncryptionService) {
        this.fileEncryptionService = fileEncryptionService;
    }

    @Operation(
        summary = "Encrypt a file",
        description = "Encrypts an uploaded file. DEK is generated per file and wrapped via Vault transit. " +
                      "Output format: [4-byte metadata length][metadata JSON][encrypted file bytes].",
        responses = {
            @ApiResponse(responseCode = "200", description = "Encrypted file stream",
                content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "400", description = "Missing or invalid request"),
            @ApiResponse(responseCode = "500", description = "Encryption or Vault failure")
        }
    )
    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> encrypt(
            @Parameter(description = "File to encrypt") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Optional encryption config (algorithm, keyId, fileName)")
            @RequestPart(value = "request", required = false) EncryptionRequest request) throws Exception {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encrypted.enc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEncryptionService.encryptFile(file, request));
    }

    @Operation(
        summary = "Decrypt a file",
        description = "Decrypts a file previously encrypted by this service. " +
                      "Algorithm and wrapped DEK are read from the file metadata header.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Decrypted file stream",
                content = @Content(mediaType = "application/octet-stream")),
            @ApiResponse(responseCode = "400", description = "Invalid or corrupted encrypted file"),
            @ApiResponse(responseCode = "500", description = "Decryption or Vault failure")
        }
    )
    @PostMapping(value = "/decrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> decrypt(
            @Parameter(description = "Encrypted .enc file") @RequestPart("file") MultipartFile file,
            @Parameter(description = "Vault transit key name. Defaults to my-key")
            @RequestParam(value = "keyId", required = false) String keyId) throws Exception {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=decrypted")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEncryptionService.decryptFile(file, keyId));
    }
}
