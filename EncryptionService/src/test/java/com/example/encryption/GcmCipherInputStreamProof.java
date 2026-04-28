package com.example.encryption;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Proves whether CipherInputStream with AES-GCM releases plaintext chunk-by-chunk
 * or buffers everything until the auth tag is verified at EOF.
 *
 * Friend's claim: "tag is verified at EOF, data might already be written before that"
 *
 * Run: mvn test-compile exec:java -Dexec.mainClass=com.example.encryption.GcmCipherInputStreamProof -Dexec.classpathScope=test
 */
public class GcmCipherInputStreamProof {

    private static final int FILE_SIZE    = 512 * 1024; // 512 KB
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES     = 12;
    private static final int READ_CHUNK   = 8 * 1024;   // 8 KB — simulate streaming reads

    public static void main(String[] args) throws Exception {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey key = kg.generateKey();
        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        byte[] plaintext  = new byte[FILE_SIZE];
        new SecureRandom().nextBytes(plaintext);
        byte[] ciphertext = encrypt(plaintext, key, iv);

        System.out.printf("Plaintext : %d KB%n", plaintext.length  / 1024);
        System.out.printf("Ciphertext: %d KB (includes 16-byte GCM tag)%n%n", ciphertext.length / 1024);

        // --- Test 1: Valid ciphertext — does data flow chunk by chunk? ---
        System.out.println("=== TEST 1: Valid ciphertext — tracking bytes released per read() ===");
        decryptTrackingChunks(ciphertext, key, iv, false);

        // --- Test 2: Tampered ciphertext — does partial plaintext leak before exception? ---
        System.out.println("\n=== TEST 2: Tampered ciphertext — does partial plaintext get written? ===");
        byte[] tampered = ciphertext.clone();
        tampered[FILE_SIZE / 2] ^= 0xFF; // flip bits in the middle of the ciphertext
        decryptTrackingChunks(tampered, key, iv, true);
    }

    private static void decryptTrackingChunks(byte[] ciphertext, SecretKey key, byte[] iv,
                                              boolean expectTamper) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));

        AtomicInteger totalWritten  = new AtomicInteger(0);
        AtomicInteger chunksWritten = new AtomicInteger(0);
        AtomicInteger readsTotal    = new AtomicInteger(0);

        // CountingOutputStream — reports every time bytes actually flow through
        OutputStream countingOut = new OutputStream() {
            @Override
            public void write(byte[] b, int off, int len) {
                if (len > 0) {
                    chunksWritten.incrementAndGet();
                    totalWritten.addAndGet(len);
                    System.out.printf("  >> write() called: %d bytes (total so far: %d KB)%n",
                            len, totalWritten.get() / 1024);
                }
            }
            @Override public void write(int b) { totalWritten.incrementAndGet(); }
        };

        try (CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(ciphertext), cipher)) {
            byte[] buf = new byte[READ_CHUNK];
            int n;
            while ((n = cis.read(buf)) != -1) {
                readsTotal.incrementAndGet();
                countingOut.write(buf, 0, n);
                System.out.printf("  read() returned %d bytes (read #%d)%n", n, readsTotal.get());
            }
            System.out.printf("%nResult: %d reads, %d write() calls, %d KB written — tag verified OK%n",
                    readsTotal.get(), chunksWritten.get(), totalWritten.get() / 1024);
        } catch (Exception e) {
            System.out.printf("%nException after %d reads and %d KB written: %s%n",
                    readsTotal.get(), totalWritten.get() / 1024, e.getMessage());
            if (expectTamper) {
                System.out.println("Friend's claim check: was ANY plaintext written before exception?");
                System.out.println("  Bytes written before exception: " + totalWritten.get());
                if (totalWritten.get() == 0) {
                    System.out.println("  => FRIEND IS WRONG: GCM buffered everything, nothing leaked");
                } else {
                    System.out.println("  => FRIEND IS RIGHT: plaintext leaked before tag verification!");
                }
            }
        }
    }

    private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(cipher.doFinal(plaintext));
        return out.toByteArray();
    }
}
