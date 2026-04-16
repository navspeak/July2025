package com.example.encryption.domain;

public record FileEncryptionMetadata(
        String originalFileName,
        String ivBase64,
        String wrappedDek,
        EncryptionAlgorithm algorithm) {}
