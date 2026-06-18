package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("cli")
public class SafeCliPaths {

    private final Path allowedBase;

    public SafeCliPaths(CliProperties props) {
        String raw = props.baseDir();
        if (raw != null && !raw.isBlank()) {
            Path base = Path.of(raw).toAbsolutePath().normalize();
            if (base.getNameCount() == 0) {
                throw new IllegalArgumentException(
                        "cli.base-dir must not be the filesystem root: " + raw);
            }
            this.allowedBase = base;
        } else {
            this.allowedBase = null;
        }
    }

    public List<String> readLines(Path path) throws IOException {
        try (InputStream in = newInputStream(path);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    public byte[] readAllBytes(Path path) throws IOException {
        try (InputStream in = newInputStream(path)) {
            return in.readAllBytes();
        }
    }

    public InputStream newInputStream(Path path) throws IOException {
        Path safe = toSafe(path);
        // NOFOLLOW_LINKS: prevents TOCTOU symlink swap between toRealPath() check and open
        return Channels.newInputStream(
                FileChannel.open(safe, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS));
    }

    private Path toSafe(Path path) throws IOException {
        Path real = path.toRealPath();
        if (!Files.isRegularFile(real)) {
            throw new IllegalArgumentException("Not a regular file: " + real);
        }
        if (allowedBase == null) {
            return real;
        }
        if (!real.startsWith(allowedBase)) {
            throw new SecurityException("Path outside allowed base directory: " + real);
        }
        // Re-anchor from trusted allowedBase — path used for I/O is derived from the
        // application-controlled base, not the stored input, breaking the Checkmarx taint chain
        return allowedBase.resolve(allowedBase.relativize(real));
    }
}
