package com.example.encryption.cli;

import com.example.encryption.domain.EncryptionAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@Profile("cli")
@Command(name = "encrypt-dir", description = "Encrypt matching files in a directory")
public class EncryptDirCommand implements Runnable {

    @Option(names = "--dir", required = true, description = "Source directory")
    private Path dir;

    @Option(names = "--out", required = true, description = "Output directory")
    private Path out;

    @Option(names = "--algorithm", description = "AES_256_GCM (default) or CHACHA20_POLY1305")
    private EncryptionAlgorithm algorithm;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    @Option(names = "--recursive", description = "Descend into subdirectories")
    private boolean recursive;

    private final EncryptionClient client;
    private final SuffixFilter filter;

    public EncryptDirCommand(EncryptionClient client, SuffixFilter filter) {
        this.client = client;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(out);

            Stream<Path> walk = recursive ? Files.walk(dir) : Files.list(dir);
            List<Path> files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> filter.matches(p.getFileName().toString()))
                    .sorted()
                    .toList();

            if (files.isEmpty()) {
                System.out.println("No matching files found in " + dir);
                return;
            }

            int succeeded = 0, failed = 0;
            for (Path file : files) {
                try {
                    byte[] encrypted = client.encryptFile(
                            Files.readAllBytes(file),
                            file.getFileName().toString(),
                            algorithm, transitKey);

                    Path relative = dir.relativize(file);
                    Path outFile = out.resolve(relative.resolveSibling(file.getFileName() + ".enc"));
                    Files.createDirectories(outFile.getParent());
                    Files.write(outFile, encrypted);
                    System.out.printf("  encrypted: %s%n", file);
                    succeeded++;
                } catch (Exception e) {
                    System.err.printf("  FAILED: %s — %s%n", file, e.getMessage());
                    log.debug("Encrypt failure detail", e);
                    failed++;
                }
            }
            System.out.printf("%nDone: %d encrypted, %d failed%n", succeeded, failed);

        } catch (Exception e) {
            throw new RuntimeException("encrypt-dir failed: " + e.getMessage(), e);
        }
    }
}
