package com.example.encryption.domain;

import java.nio.file.Path;

public record StagingPath(Path inputPath, Path outputPath, String traceId) {}
