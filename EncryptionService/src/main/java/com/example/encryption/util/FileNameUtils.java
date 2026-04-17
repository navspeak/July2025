package com.example.encryption.util;

import java.nio.file.Path;
import java.util.regex.Pattern;

public final class FileNameUtils {

    private static final Pattern UNSAFE_CHARS = Pattern.compile("[^a-zA-Z0-9._-]");
    private static final int MAX_LENGTH = 64;

    private FileNameUtils() {}

    public static String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) return "unknown";
        String name = Path.of(fileName).getFileName().toString();
        name = UNSAFE_CHARS.matcher(name).replaceAll("_");
        return name.length() > MAX_LENGTH ? name.substring(0, MAX_LENGTH) : name;
    }
}
