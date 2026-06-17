package com.example.encryption.processor;

import com.example.encryption.domain.EncryptionAlgorithm;
import com.example.encryption.domain.FileEncryptionContext;
import com.example.encryption.domain.FileEncryptionMetadata;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptionProcessor {

    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final int KEY_BITS = 256;

    private final SecureRandom random = new SecureRandom();

    public FileEncryptionContext initEncrypt(EncryptionAlgorithm algorithm) throws Exception {
        byte[] keyBytes = generateKey(algorithm);
        return initEncryptWithKey(keyBytes, algorithm);
    }

    public byte[] generateKey(EncryptionAlgorithm algorithm) throws Exception {
        return switch (algorithm) {
            case AES_256_GCM -> {
                var kg = KeyGenerator.getInstance("AES");
                kg.init(KEY_BITS);
                SecretKey key = kg.generateKey();
                yield key.getEncoded();
            }
            case CHACHA20_POLY1305 -> {
                byte[] key = new byte[32];
                random.nextBytes(key);
                yield key;
            }
        };
    }

    public FileEncryptionContext initEncryptWithKey(byte[] keyBytes, EncryptionAlgorithm algorithm) throws Exception {
        byte[] iv = new byte[IV_BYTES];
        random.nextBytes(iv);
        Cipher cipher = buildEncryptCipher(algorithm, keyBytes, iv);
        return new FileEncryptionContext(cipher, iv, Base64.getEncoder().encodeToString(keyBytes));
    }

    public Cipher initDecryptCipher(FileEncryptionMetadata metadata, String dekBase64) throws Exception {
        byte[] iv = Base64.getDecoder().decode(metadata.ivBase64());
        byte[] dekBytes = Base64.getDecoder().decode(dekBase64);
        return buildDecryptCipher(metadata.algorithm(), dekBytes, iv);
    }

    private Cipher buildEncryptCipher(EncryptionAlgorithm algorithm, byte[] keyBytes, byte[] iv) throws Exception {
        return switch (algorithm) {
            case AES_256_GCM -> {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(keyBytes, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                yield cipher;
            }
            case CHACHA20_POLY1305 -> {
                Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
                cipher.init(Cipher.ENCRYPT_MODE,
                        new SecretKeySpec(keyBytes, "ChaCha20"),
                        new IvParameterSpec(iv));
                yield cipher;
            }
        };
    }

    private Cipher buildDecryptCipher(EncryptionAlgorithm algorithm, byte[] keyBytes, byte[] iv) throws Exception {
        return switch (algorithm) {
            case AES_256_GCM -> {
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE,
                        new SecretKeySpec(keyBytes, "AES"),
                        new GCMParameterSpec(GCM_TAG_BITS, iv));
                yield cipher;
            }
            case CHACHA20_POLY1305 -> {
                Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");
                cipher.init(Cipher.DECRYPT_MODE,
                        new SecretKeySpec(keyBytes, "ChaCha20"),
                        new IvParameterSpec(iv));
                yield cipher;
            }
        };
    }
}
