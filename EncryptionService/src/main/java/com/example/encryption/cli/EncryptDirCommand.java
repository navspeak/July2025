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

    @Option(names = "--same-dek", description = "Share one DEK across all files (one key-wrap call)")
    private boolean sameDek;

    @Option(names = "--recursive", description = "Descend into subdirectories")
    private boolean recursive;

    private final PathEncryptionService encryptionService;
    private final SuffixFilter filter;
    private final FailureLogger failureLogger;

    public EncryptDirCommand(PathEncryptionService encryptionService,
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

            if (sameDek) {
                runSameDek(files);
            } else {
                runPerFile(files);
            }

        } catch (Exception e) {
            throw new RuntimeException("encrypt-dir failed: " + e.getMessage(), e);
        }
    }

    private void runPerFile(List<Path> files) {
        int succeeded = 0, failed = 0;
        for (Path file : files) {
            Path outFile = resolveOutput(file);
            try {
                Files.createDirectories(outFile.getParent());
                encryptionService.encryptFile(file, outFile, algorithm, transitKey);
                System.out.printf("  encrypted: %s%n", file);
                succeeded++;
            } catch (Exception e) {
                System.err.printf("  FAILED: %s — %s%n", file, e.getMessage());
                failureLogger.log(file.toString(), e.getMessage());
                log.debug("Encrypt failure detail", e);
                failed++;
            }
        }
        System.out.printf("%nDone: %d encrypted, %d failed%n", succeeded, failed);
    }

    private void runSameDek(List<Path> files) throws Exception {
        List<PathEncryptionService.FileEncryptTask> tasks = files.stream()
                .map(f -> new PathEncryptionService.FileEncryptTask(f, resolveOutput(f)))
                .toList();
        tasks.forEach(t -> {
            try { Files.createDirectories(t.output().getParent()); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        encryptionService.encryptFiles(tasks, algorithm, transitKey);
        System.out.printf("Done: %d encrypted with shared DEK%n", tasks.size());
    }

    private Path resolveOutput(Path file) {
        return out.resolve(dir.relativize(file)).resolveSibling(file.getFileName() + ".enc");
    }
}
