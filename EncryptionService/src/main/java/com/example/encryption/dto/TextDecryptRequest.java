package com.example.encryption.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TextDecryptRequest(
        @Schema(description = "Vault ciphertext token (vault:v1:...)", requiredMode = Schema.RequiredMode.REQUIRED)
        String ciphertext,

        @Schema(description = "Vault transit key name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String transitKey) {
}
