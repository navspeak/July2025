package com.example.encryption.cli;

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
@Command(name = "decrypt-list", description = "Decrypt .enc files listed in a text file (one path per line)")
public class DecryptListCommand implements Runnable {

    @Option(names = "--list", required = true, description = "Text file with one .enc file path per line")
    private Path list;

    @Option(names = "--out", required = true, description = "Output directory for decrypted files")
    private Path out;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    private final PathEncryptionService encryptionService;
    private final FailureLogger failureLogger;
    private final ProgressBar progressBar;

    public DecryptListCommand(PathEncryptionService encryptionService,
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
                    .map(Path::of)
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

            int succeeded = 0, failed = 0;
            progressBar.start(files.size());
            for (Path file : files) {
                String outName = stripEnc(file.getFileName().toString());
                Path outFile = out.resolve(outName);
                try {
                    encryptionService.decryptFile(file, outFile, transitKey);
                    succeeded++;
                } catch (Exception e) {
                    failureLogger.log(file.toString(), e.getMessage());
                    log.debug("Decrypt failure detail", e);
                    failed++;
                }
                progressBar.advance(file.getFileName().toString());
            }
            System.out.printf("Done: %d decrypted, %d failed%n", succeeded, failed);

        } catch (Exception e) {
            throw new RuntimeException("decrypt-list failed: " + e.getMessage(), e);
        }
    }

    private String stripEnc(String filename) {
        return filename.endsWith(".enc") ? filename.substring(0, filename.length() - 4) : filename;
    }
}
