package com.example.encryption.runner;

import java.io.IOException;
import java.nio.file.Path;

public interface FileEncryptionClient {
    EncryptionResult encrypt(Path inputFile, String keyId, String algorithm) throws IOException;
    EncryptionResult decrypt(Path encryptedFile, String keyId) throws IOException;
}
