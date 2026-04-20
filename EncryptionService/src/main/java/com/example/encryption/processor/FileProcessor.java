package com.example.encryption.processor;

import com.example.encryption.domain.Operation;
import com.example.encryption.domain.StagingPath;
import com.example.encryption.util.FileNameUtils;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
@Profile("!client")
public class FileProcessor {

    @Value("${staging.dir:/tmp/encryption-staging}")
    private String stagingDir;

    private final Tracer tracer;

    public FileProcessor(Tracer tracer) {
        this.tracer = tracer;
    }

    public StagingPath resolvePaths(MultipartFile file, Operation op) throws IOException {
        Span currentSpan = tracer.currentSpan();
        String traceId = currentSpan != null
                ? currentSpan.context().traceId()
                : UUID.randomUUID().toString();

        Path baseDir = Path.of(stagingDir);
        Files.createDirectories(baseDir);

        Path inputPath = baseDir.resolve(traceId + "-input-" + FileNameUtils.sanitize(file.getOriginalFilename()));
        String suffix = op == Operation.ENCRYPT ? ".enc" : ".dec";
        Path outputPath = baseDir.resolve(traceId + "-output" + suffix);

        return new StagingPath(inputPath, outputPath, traceId);
    }

    public void stageInput(MultipartFile file, Path inputPath) throws IOException {
        file.transferTo(inputPath);
    }

    public void cleanup(StagingPath paths) {
        try { Files.deleteIfExists(paths.inputPath()); } catch (Exception ignored) {}
        try { Files.deleteIfExists(paths.outputPath()); } catch (Exception ignored) {}
    }
}
