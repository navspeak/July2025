package com.example.encryption.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

@Component
public class StagingCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(StagingCleanupJob.class);

    @Value("${staging.dir:/tmp/encryption-staging}")
    private String stagingDir;

    @Value("${staging.cleanup.max-age-minutes:60}")
    private long maxAgeMinutes;

    // runs every hour
    @Scheduled(fixedDelayString = "${staging.cleanup.interval-ms:3600000}")
    public void cleanupOrphanedFiles() {
        Path baseDir = Path.of(stagingDir);
        if (!Files.exists(baseDir)) return;

        Instant cutoff = Instant.now().minus(maxAgeMinutes, ChronoUnit.MINUTES);
        try (Stream<Path> files = Files.list(baseDir)) {
            files.filter(path -> isOlderThan(path, cutoff))
                 .forEach(this::deleteQuietly);
        } catch (IOException e) {
            log.warn("Staging cleanup failed: {}", e.getMessage());
        }
    }

    private boolean isOlderThan(Path path, Instant cutoff) {
        try {
            BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
            return attrs.lastModifiedTime().toInstant().isBefore(cutoff);
        } catch (IOException e) {
            return false;
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
            log.info("Cleaned up orphaned staging file: {}", path.getFileName());
        } catch (IOException e) {
            log.warn("Could not delete staging file {}: {}", path.getFileName(), e.getMessage());
        }
    }
}
