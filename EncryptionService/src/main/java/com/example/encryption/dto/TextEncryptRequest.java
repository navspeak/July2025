package com.example.encryption.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TextEncryptRequest(
        @Schema(description = "Plaintext to encrypt", requiredMode = Schema.RequiredMode.REQUIRED)
        String plaintext,

        @Schema(description = "Vault transit key name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String transitKey) {
}
