package com.example.encryption.dto;

import com.example.encryption.domain.EncryptionAlgorithm;

public class EncryptionRequest {

    private EncryptionAlgorithm algorithm = EncryptionAlgorithm.AES_256_GCM;
    private String keyId;
    private String fileName;

    public EncryptionAlgorithm getAlgorithm() { return algorithm; }
    public void setAlgorithm(EncryptionAlgorithm algorithm) { this.algorithm = algorithm; }

    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
