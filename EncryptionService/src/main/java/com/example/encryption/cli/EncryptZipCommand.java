package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

    @Option(names = "--same-dek", description = "Share one DEK across all entries (one key-wrap call)")
    private boolean sameDek;

    private final PathEncryptionService encryptionService;
    private final SuffixFilter filter;
    private final FailureLogger failureLogger;

    public EncryptZipCommand(PathEncryptionService encryptionService,
                             SuffixFilter filter,
                             FailureLogger failureLogger) {
        this.encryptionService = encryptionService;
        this.filter = filter;
        this.failureLogger = failureLogger;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(out);

            // Read all matching entries into memory first (ZipInputStream is forward-only)
            record ZipEntryData(String name, byte[] bytes) {}
            List<ZipEntryData> entries = new ArrayList<>();

            try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip))) {
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    String name = Path.of(entry.getName()).getFileName().toString();
                    if (!entry.isDirectory() && filter.matches(name)) {
                        entries.add(new ZipEntryData(name, zin.readAllBytes()));
                    }
                    zin.closeEntry();
                }
            }

            if (entries.isEmpty()) {
                System.out.println("No matching entries found in " + zip);
                return;
            }

            if (sameDek) {
                // Write entries to temp files, bulk-encrypt, clean up
                List<PathEncryptionService.FileEncryptTask> tasks = new ArrayList<>();
                List<Path> tempFiles = new ArrayList<>();
                for (ZipEntryData entry : entries) {
                    Path tmp = Files.createTempFile("enc-zip-", "-" + entry.name());
                    Files.write(tmp, entry.bytes());
                    tempFiles.add(tmp);
                    tasks.add(new PathEncryptionService.FileEncryptTask(tmp, out.resolve(entry.name() + ".enc")));
                }
                try {
                    encryptionService.encryptFiles(tasks, algorithm, transitKey);
                    entries.forEach(e -> System.out.printf("  encrypted: %s%n", e.name()));
                    System.out.printf("%nDone: %d encrypted with shared DEK%n", entries.size());
                } finally {
                    tempFiles.forEach(t -> { try { Files.deleteIfExists(t); } catch (Exception ignored) {} });
                }
            } else {
                int succeeded = 0, failed = 0;
                for (ZipEntryData entry : entries) {
                    Path tmp = Files.createTempFile("enc-zip-", "-" + entry.name());
                    try {
                        Files.write(tmp, entry.bytes());
                        encryptionService.encryptFile(tmp, out.resolve(entry.name() + ".enc"), algorithm, transitKey);
                        System.out.printf("  encrypted: %s%n", entry.name());
                        succeeded++;
                    } catch (Exception e) {
                        System.err.printf("  FAILED: %s — %s%n", entry.name(), e.getMessage());
                        failureLogger.log(entry.name(), e.getMessage());
                        log.debug("Encrypt failure detail", e);
                        failed++;
                    } finally {
                        Files.deleteIfExists(tmp);
                    }
                }
                System.out.printf("%nDone: %d encrypted, %d failed%n", succeeded, failed);
            }

        } catch (Exception e) {
            throw new RuntimeException("encrypt-zip failed: " + e.getMessage(), e);
        }
    }
}
