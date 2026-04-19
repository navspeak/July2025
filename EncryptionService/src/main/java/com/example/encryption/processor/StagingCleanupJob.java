package com.example.encryption.processor;

import lombok.extern.slf4j.Slf4j;
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

/**
 * Cleans up orphaned staging files left behind by abnormal JVM termination.
 *
 * Normal flow: staging files (input + output) are created per request under
 * staging.dir, and deleted in the finally block of StreamingResponseBody after
 * the response has been fully streamed to the client. Under normal operation no
 * files accumulate.
 *
 * Orphan scenario: if the JVM crashes or is killed mid-stream, the finally block
 * never runs and staging files are left on disk indefinitely. This job runs hourly
 * and deletes any file whose creation time is older than max-age-minutes (default 60).
 *
 * creationTime is used (not lastModifiedTime) because staging files are written once
 * and never modified — lastModifiedTime could be unreliable on some filesystems.
 */
@Slf4j
@Component
public class StagingCleanupJob {
    private static final long CLEANUP_INTERVAL_MS = 3_600_000; // 1 hour
    private static final long MAX_AGE_MINUTES = 60;

    @Value("${staging.dir:/tmp/encryption-staging}")
    private String stagingDir;

    @Scheduled(fixedDelay = CLEANUP_INTERVAL_MS)
    public void cleanupOrphanedFiles() {
        Path baseDir = Path.of(stagingDir);
        if (!Files.exists(baseDir)) return;

        Instant cutoff = Instant.now().minus(MAX_AGE_MINUTES, ChronoUnit.MINUTES);
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
            return attrs.creationTime().toInstant().isBefore(cutoff);
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
