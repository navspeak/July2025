package com.example.encryption.cli;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

@Slf4j
@Component
@Profile("cli")
public class FailureLogger {

    private static final String FILE_NAME = "failed_encryption.txt";

    private final Path logFile;

    public FailureLogger(CliProperties props) {
        String logDir = props.logDir();
        logFile = (logDir != null && !logDir.isBlank())
                ? Path.of(logDir, FILE_NAME)
                : Path.of(FILE_NAME);
    }

    public void log(String filePath, String error) {
        String line = Instant.now() + " | " + filePath + " | " + error + System.lineSeparator();
        try {
            Files.createDirectories(logFile.getParent() == null ? Path.of(".") : logFile.getParent());
            Files.writeString(logFile, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.error("Could not write to {}: {}", logFile, e.getMessage());
        }
    }

    public Path logFile() {
        return logFile;
    }
}
