package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Profile("cli")
public class SafeCliPaths {

    private final Path allowedBase;

    public SafeCliPaths(CliProperties props) {
        String raw = props.baseDir();
        this.allowedBase = (raw != null && !raw.isBlank())
                ? Path.of(raw).toAbsolutePath().normalize()
                : null;
    }

    public byte[] readAllBytes(Path path) throws IOException {
        return Files.readAllBytes(toSafe(path));
    }

    public InputStream newInputStream(Path path) throws IOException {
        return Files.newInputStream(toSafe(path));
    }

    private Path toSafe(Path path) throws IOException {
        Path real = path.toRealPath();
        if (!Files.isRegularFile(real)) {
            throw new IllegalArgumentException("Not a regular file: " + real);
        }
        if (allowedBase != null && !real.startsWith(allowedBase)) {
            throw new SecurityException("Path outside allowed base directory: " + real);
        }
        return real;
    }
}
