package com.example.encryption.processor;

import com.example.encryption.domain.Operation;
import com.example.encryption.domain.StagingPath;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Component
@Profile("!cli")
public class FileProcessor {

    @Value("${staging.dir:/tmp/encryption-staging}")
    private String stagingDir;

    private final Tracer tracer;

    public FileProcessor(Tracer tracer) {
        this.tracer = tracer;
    }

    public StagingPath resolvePaths(Operation op) throws IOException {
        Span currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null
                ? currentSpan.context().traceId()
                : UUID.randomUUID().toString();

        Path baseDir = Path.of(stagingDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);

        // Staging filenames are trace-id based only — no user input in path
        Path inputPath = baseDir.resolve(traceId + "-input");
        String suffix = op == Operation.ENCRYPT ? ".enc" : ".dec";
        Path outputPath = baseDir.resolve(traceId + "-output" + suffix);

        assertWithinStaging(inputPath, baseDir);
        assertWithinStaging(outputPath, baseDir);

        return new StagingPath(inputPath, outputPath, traceId);
    }

    private void assertWithinStaging(Path path, Path baseDir) {
        if (!path.normalize().startsWith(baseDir)) {
            throw new SecurityException("Path traversal detected: " + path);
        }
    }

    public void stageInput(MultipartFile file, Path inputPath) throws IOException {
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, inputPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public byte[] readInput(StagingPath paths) throws IOException {
        Path baseDir = Path.of(stagingDir).toAbsolutePath().normalize();
        assertWithinStaging(paths.inputPath().toAbsolutePath().normalize(), baseDir);
        return Files.readAllBytes(paths.inputPath());
    }

    public java.io.InputStream openInput(StagingPath paths) throws IOException {
        Path baseDir = Path.of(stagingDir).toAbsolutePath().normalize();
        assertWithinStaging(paths.inputPath().toAbsolutePath().normalize(), baseDir);
        return Files.newInputStream(paths.inputPath());
    }

    public void cleanup(StagingPath paths) {
        try { Files.deleteIfExists(paths.inputPath()); } catch (Exception ignored) {}
        try { Files.deleteIfExists(paths.outputPath()); } catch (Exception ignored) {}
    }
}
