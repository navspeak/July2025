package com.example.encryption.cli;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cli")
public record CliProperties(
        String serviceUrl,
        boolean skipSslVerify,
        Jwt jwt,
        String suffixToEncrypt,
        String logDir
) {
    public record Jwt(String tokenUri, String clientId, String clientSecret) {}
}
