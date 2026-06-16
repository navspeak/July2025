package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@Component
@Profile("cli")
@Command(name = "encrypt-zip", description = "Encrypt matching entries inside a ZIP archive")
public class EncryptZipCommand implements Runnable {

    @Option(names = "--zip", required = true, description = "Source ZIP archive")
    private Path zip;

    @Option(names = "--out", required = true, description = "Output directory for .enc files")
    private Path out;

    @Option(names = "--algorithm", description = "AES_256_GCM (default) or CHACHA20_POLY1305")
    private EncryptionAlgorithm algorithm;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final EncryptionClient client;
    private final SuffixFilter filter;

    public EncryptZipCommand(EncryptionClient client, SuffixFilter filter) {
        this.client = client;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(out);

            int succeeded = 0, failed = 0;
            try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip))) {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = Path.of(entry.getName()).getFileName().toString();
                    if (entry.isDirectory() || !filter.matches(entryName)) {
                        zin.closeEntry();
                        continue;
                    }
                    try {
                        byte[] encrypted = client.encryptFile(
                                zin.readAllBytes(), entryName, algorithm, transitKey);
                        Files.write(out.resolve(entryName + ".enc"), encrypted);
                        System.out.printf("  encrypted: %s%n", entryName);
                        succeeded++;
                    } catch (Exception e) {
                        System.err.printf("  FAILED: %s — %s%n", entryName, e.getMessage());
                        log.debug("Encrypt failure detail", e);
                        failed++;
                    }
                    zin.closeEntry();
                }
            }
            System.out.printf("%nDone: %d encrypted, %d failed%n", succeeded, failed);

        } catch (Exception e) {
            throw new RuntimeException("encrypt-zip failed: " + e.getMessage(), e);
        }
    }
}
