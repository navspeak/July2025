package com.example.encryption.controller;

final class ApiDocs {

    static final String TAG_FILE    = "File Encryption";
    static final String TAG_TEXT    = "Text Encryption";
    static final String TAG_SECRETS = "Vault KV Secrets";

    // File endpoints
    static final String FILE_ENCRYPT_SUMMARY = "Encrypt a file";
    static final String FILE_ENCRYPT_DESC    = "Encrypts an uploaded file using AES-256-GCM or ChaCha20-Poly1305. " +
            "A DEK is generated per file and wrapped via Vault transit. " +
            "Returns a binary stream: [4-byte metadata length][metadata JSON][encrypted bytes].";
    static final String FILE_DECRYPT_SUMMARY = "Decrypt a file";
    static final String FILE_DECRYPT_DESC    = "Decrypts a .enc file produced by this service. " +
            "Algorithm, IV and wrapped DEK are read from the embedded metadata header — no extra parameters needed.";

    // Text endpoints
    static final String TEXT_ENCRYPT_SUMMARY = "Encrypt plaintext via Vault transit";
    static final String TEXT_DECRYPT_SUMMARY = "Decrypt ciphertext via Vault transit";

    // Secret endpoints
    static final String SECRET_WRITE_SUMMARY    = "Write secret at path";
    static final String SECRET_READ_SUMMARY     = "Read all keys at path";
    static final String SECRET_READ_KEY_SUMMARY = "Read single key at path";
    static final String SECRET_DELETE_SUMMARY   = "Delete secret at path";

    // Response descriptions
    static final String RESP_200_STREAM = "Encrypted or decrypted binary stream";
    static final String RESP_204        = "No Content";
    static final String RESP_400        = "Missing required field";
    static final String RESP_404        = "Secret path not found";
    static final String RESP_413        = "File exceeds 1 MB limit";
    static final String RESP_500        = "Vault unreachable or encryption failure";

    private ApiDocs() {}
}
