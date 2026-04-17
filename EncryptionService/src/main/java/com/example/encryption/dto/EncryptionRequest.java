package com.example.encryption.dto;

import com.example.encryption.domain.EncryptionAlgorithm;

public record EncryptionRequest(
        EncryptionAlgorithm algorithm,
        String transitKey,
        String fileName) {

    public EncryptionRequest {
        algorithm = algorithm != null ? algorithm : EncryptionAlgorithm.AES_256_GCM;
    }
}
