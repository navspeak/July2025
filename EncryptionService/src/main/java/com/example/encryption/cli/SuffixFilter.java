package com.example.encryption.cli;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Profile("cli")
public class SuffixFilter {

    private final Set<String> suffixes;

    public SuffixFilter(CliProperties props) {
        String raw = props.suffixToEncrypt();
        if (raw == null || raw.isBlank()) {
            suffixes = Set.of();
        } else {
            suffixes = Arrays.stream(raw.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
        }
    }

    public boolean matches(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".enc")) return false;
        if (suffixes.isEmpty()) return true;
        return suffixes.stream().anyMatch(lower::endsWith);
    }
}
