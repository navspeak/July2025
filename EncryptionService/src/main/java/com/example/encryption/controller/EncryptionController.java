package com.example.encryption.controller;

import com.example.encryption.dto.EncryptionRequest;
import com.example.encryption.service.FileEncryptionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequestMapping("/api/v1/file")
public class EncryptionController {

    private final FileEncryptionService fileEncryptionService;

    public EncryptionController(FileEncryptionService fileEncryptionService) {
        this.fileEncryptionService = fileEncryptionService;
    }

    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> encrypt(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "request", required = false) EncryptionRequest request) throws Exception {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encrypted.enc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEncryptionService.encryptFile(file, request));
    }

    @PostMapping(value = "/decrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> decrypt(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "transitKey", required = false) String transitKey) throws Exception {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=decrypted")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEncryptionService.decryptFile(file, transitKey));
    }
}
