package com.example.encryption.domain;

import java.util.Arrays;

public enum EncryptionAlgorithm {
    AES_256_GCM("AES-256-GCM"),
    CHACHA20_POLY1305("CHACHA20-POLY1305");

    private final String value;

    EncryptionAlgorithm(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static EncryptionAlgorithm fromValue(String value) {
        return Arrays.stream(values())
                .filter(a -> a.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported algorithm: " + value));
    }
}
