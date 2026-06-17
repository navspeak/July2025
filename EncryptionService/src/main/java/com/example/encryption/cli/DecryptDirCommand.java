package com.example.encryption.cli;

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
@Command(name = "decrypt-dir", description = "Decrypt all .enc files in a directory")
public class DecryptDirCommand implements Runnable {

    @Option(names = "--dir", required = true, description = "Source directory containing .enc files")
    private Path dir;

    @Option(names = "--out", required = true, description = "Output directory for decrypted files")
    private Path out;

    @Option(names = "--transit-key", description = "Vault transit key name")
    private String transitKey;

    @Option(names = "--recursive", description = "Descend into subdirectories")
    private boolean recursive;

    private final PathEncryptionService encryptionService;
    private final FailureLogger failureLogger;
    private final ProgressBar progressBar;

    public DecryptDirCommand(PathEncryptionService encryptionService,
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

            Stream<Path> walk = recursive ? Files.walk(dir) : Files.list(dir);
            List<Path> files = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".enc"))
                    .sorted()
                    .toList();

            if (files.isEmpty()) {
                System.out.println("No .enc files found in " + dir);
                return;
            }

            int succeeded = 0, failed = 0;
            progressBar.start(files.size());
            for (Path file : files) {
                String outName = stripEnc(file.getFileName().toString());
                Path relative = dir.relativize(file);
                Path outFile = out.resolve(relative.resolveSibling(outName));
                try {
                    Files.createDirectories(outFile.getParent());
                    encryptionService.decryptFile(file, outFile, transitKey);
                    succeeded++;
                } catch (Exception e) {
                    failureLogger.log(file.toString(), e.getMessage());
                    log.debug("Decrypt failure detail for {}", file, e);
                    failed++;
                }
                progressBar.advance(file.getFileName().toString());
            }
            System.out.printf("Done: %d decrypted, %d failed%n", succeeded, failed);

        } catch (Exception e) {
            throw new RuntimeException("decrypt-dir failed: " + e.getMessage(), e);
        }
    }

    private String stripEnc(String filename) {
        return filename.endsWith(".enc") ? filename.substring(0, filename.length() - 4) : filename;
    }
}
