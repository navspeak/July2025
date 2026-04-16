package com.example.encryption.domain;

import javax.crypto.Cipher;

public record FileEncryptionContext(Cipher cipher, byte[] iv, String dekBase64) {}
