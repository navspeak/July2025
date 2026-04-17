package com.example.encryption.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;

@ConfigurationProperties(prefix = "app.vault")
public record VaultProperties(
        String url,
        String namespace,
        String roleId,
        String secretId,
        String transitMountPath,
        String kvMountPath,
        String transitKey,
        Duration readTimeout) {

    public VaultProperties {
        readTimeout = readTimeout != null ? readTimeout : Duration.ofSeconds(5);
    }
}
