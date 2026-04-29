package com.example.encryption.client;

import java.nio.file.Path;

record EncryptionResult(Path path, String traceId, int status) {
    boolean isSuccess() { return status >= 200 && status < 300; }
}
