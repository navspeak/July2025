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

@Slf4j
@Component
@Profile("cli")
@Command(name = "encrypt-list", description = "Encrypt files listed in a text file (one path per line)")
public class EncryptListCommand implements Runnable {

    @Option(names = "--list", required = true, description = "Text file with one source file path per line")
    private Path list;

    @Option(names = "--out", required = true, description = "Output directory for .enc files")
    private Path out;

    @Option(names = "--algorithm", description = "AES_256_GCM (default) or CHACHA20_POLY1305")
    private EncryptionAlgorithm algorithm;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    @Option(names = "--same-dek", description = "Share one DEK across all files (one key-wrap call)")
    private boolean sameDek;

    private final PathEncryptionService encryptionService;
    private final FailureLogger failureLogger;
    private final ProgressBar progressBar;

    public EncryptListCommand(PathEncryptionService encryptionService,
                              FailureLogger failureLogger,
                              ProgressBar progressBar) {
        this.encryptionService = encryptionService;
        this.failureLogger = failureLogger;
        this.progressBar = progressBar;
    }

    @Override
    public void run() {
        try {
            Files.createDirectories(out);

            List<Path> files = Files.readAllLines(list).stream()
                    .map(String::trim)
                    .filter(line -> !line.isBlank() && !line.startsWith("#"))
                    .map(line -> Path.of(line).toAbsolutePath().normalize())
                    .filter(p -> {
                        if (!Files.isRegularFile(p)) {
                            System.err.printf("  SKIPPED (not found): %s%n", p);
                            failureLogger.log(p.toString(), "file not found");
                            return false;
                        }
                        return true;
                    })
                    .toList();

            if (files.isEmpty()) {
                System.out.println("No valid files found in " + list);
                return;
            }

            if (sameDek) {
                List<PathEncryptionService.FileEncryptTask> tasks = files.stream()
                        .map(f -> new PathEncryptionService.FileEncryptTask(f, out.resolve(f.getFileName() + ".enc")))
                        .toList();
                progressBar.start(tasks.size());
                encryptionService.encryptFiles(tasks, algorithm, transitKey);
                tasks.forEach(t -> progressBar.advance(t.input().getFileName().toString()));
                System.out.printf("Done: %d encrypted with shared DEK%n", tasks.size());
            } else {
                int succeeded = 0, failed = 0;
                progressBar.start(files.size());
                for (Path file : files) {
                    Path outFile = out.resolve(file.getFileName() + ".enc");
                    try {
                        encryptionService.encryptFile(file, outFile, algorithm, transitKey);
                        succeeded++;
                    } catch (Exception e) {
                        failureLogger.log(file.toString(), e.getMessage());
                        log.debug("Encrypt failure detail", e);
                        failed++;
                    }
                    progressBar.advance(file.getFileName().toString());
                }
                System.out.printf("Done: %d encrypted, %d failed%n", succeeded, failed);
            }

        } catch (Exception e) {
            throw new RuntimeException("encrypt-list failed: " + e.getMessage(), e);
        }
    }
}
