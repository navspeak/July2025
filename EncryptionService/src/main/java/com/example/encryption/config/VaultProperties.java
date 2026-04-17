package com.example.encryption.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vault")
public record VaultProperties(
        String url,
        String roleId,
        String secretId,
        String mountPath,
        String keyName) {}
