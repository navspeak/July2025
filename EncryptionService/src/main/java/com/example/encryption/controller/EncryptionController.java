package com.example.encryption.controller;

import com.example.encryption.dto.EncryptionRequest;
import com.example.encryption.service.FileEncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Tag(name = ApiDocs.TAG_FILE)
@RestController
@RequestMapping("/api/v1/file")
public class EncryptionController {

    private final FileEncryptionService fileEncryptionService;

    public EncryptionController(FileEncryptionService fileEncryptionService) {
        this.fileEncryptionService = fileEncryptionService;
    }

    @Operation(summary = ApiDocs.FILE_ENCRYPT_SUMMARY, description = ApiDocs.FILE_ENCRYPT_DESC)
    @ApiResponse(responseCode = "200", description = ApiDocs.RESP_200_STREAM)
    @ApiResponse(responseCode = "413", description = ApiDocs.RESP_413)
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
    @PostMapping(value = "/encrypt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StreamingResponseBody> encrypt(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "request", required = false) EncryptionRequest request) throws Exception {

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encrypted.enc")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(fileEncryptionService.encryptFile(file, request));
    }

    @Operation(summary = ApiDocs.FILE_DECRYPT_SUMMARY, description = ApiDocs.FILE_DECRYPT_DESC)
    @ApiResponse(responseCode = "200", description = ApiDocs.RESP_200_STREAM)
    @ApiResponse(responseCode = "500", description = ApiDocs.RESP_500)
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
