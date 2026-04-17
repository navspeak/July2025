package com.example.encryption.dto;

import com.example.encryption.domain.EncryptionAlgorithm;

public record EncryptionRequest(
        EncryptionAlgorithm algorithm,
        String transitKey) {

    public EncryptionRequest {
        algorithm = algorithm != null ? algorithm : EncryptionAlgorithm.AES_256_GCM;
    }
}
