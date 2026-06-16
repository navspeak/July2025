package com.example.encryption.cli;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cli")
public record CliProperties(
        String serviceUrl,
        Jwt jwt,
        String suffixToEncrypt
) {
    public record Jwt(String tokenUri, String clientId, String clientSecret) {}
}
