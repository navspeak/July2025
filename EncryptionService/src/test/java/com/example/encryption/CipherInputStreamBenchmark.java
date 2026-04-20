package com.example.encryption;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Proves that CipherInputStream is significantly slower than Cipher.doFinal()
 * for AES-GCM decryption, especially at larger file sizes.
 *
 * Run: mvn test-compile exec:java -Dexec.mainClass=com.example.encryption.CipherInputStreamBenchmark -Dexec.classpathScope=test
 */
public class CipherInputStreamBenchmark {

    private static final int FILE_SIZE  = 900 * 1024; // 900 KB
    private static final int ITERATIONS = 50;
    private static final int WARMUP     = 10;
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_BYTES   = 12;

    public static void main(String[] args) throws Exception {
        System.out.printf("File size: %d KB | Iterations: %d | Warmup: %d%n%n",
                FILE_SIZE / 1024, ITERATIONS, WARMUP);

        // Generate key and IV
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey key = kg.generateKey();

        byte[] iv = new byte[IV_BYTES];
        new SecureRandom().nextBytes(iv);

        // Generate plaintext
        byte[] plaintext = new byte[FILE_SIZE];
        new SecureRandom().nextBytes(plaintext);

        // Encrypt once to get ciphertext
        byte[] ciphertext = encrypt(plaintext, key, iv);
        System.out.printf("Ciphertext size: %d KB%n%n", ciphertext.length / 1024);

        // --- Warmup ---
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP; i++) {
            decryptWithCipherInputStream(ciphertext, key, iv);
            decryptWithDoFinal(ciphertext, key, iv);
        }

        // --- Benchmark CipherInputStream ---
        List<Long> streamLatencies = new ArrayList<>(ITERATIONS);
        for (int i = 0; i < ITERATIONS; i++) {
            long t = System.nanoTime();
            decryptWithCipherInputStream(ciphertext, key, iv);
            streamLatencies.add((System.nanoTime() - t) / 1_000_000);
        }

        // --- Benchmark doFinal ---
        List<Long> doFinalLatencies = new ArrayList<>(ITERATIONS);
        for (int i = 0; i < ITERATIONS; i++) {
            long t = System.nanoTime();
            decryptWithDoFinal(ciphertext, key, iv);
            doFinalLatencies.add((System.nanoTime() - t) / 1_000_000);
        }

        printStats("CipherInputStream", streamLatencies);
        printStats("Cipher.doFinal   ", doFinalLatencies);
    }

    private static byte[] encrypt(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (CipherOutputStream cos = new CipherOutputStream(out, cipher)) {
            cos.write(plaintext);
        }
        return out.toByteArray();
    }

    private static byte[] decryptWithCipherInputStream(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        ByteArrayOutputStream out = new ByteArrayOutputStream(FILE_SIZE);
        try (InputStream in = new CipherInputStream(new ByteArrayInputStream(ciphertext), cipher)) {
            in.transferTo(out);
        }
        return out.toByteArray();
    }

    private static byte[] decryptWithDoFinal(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
        return cipher.doFinal(ciphertext);
    }

    private static void printStats(String label, List<Long> latencies) {
        Collections.sort(latencies);
        double avg = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        long p50 = percentile(latencies, 50);
        long p95 = percentile(latencies, 95);
        long p99 = percentile(latencies, 99);
        long max = latencies.get(latencies.size() - 1);

        System.out.printf("%-20s  avg=%-6.1f  p50=%-5d  p95=%-5d  p99=%-5d  max=%d  (ms)%n",
                label, avg, p50, p95, p99, max);
    }

    private static long percentile(List<Long> sorted, int pct) {
        int idx = (int) Math.ceil(pct / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(idx, sorted.size() - 1)));
    }
}
