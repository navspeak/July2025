package com.example.encryption.client;

import java.io.IOException;
import java.nio.file.Path;

public interface FileEncryptionClient {
    Path encrypt(Path inputFile, String keyId, String algorithm) throws IOException;
    Path decrypt(Path encryptedFile, String keyId) throws IOException;
}
