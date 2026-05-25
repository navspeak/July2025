package com.example.encryption.runner;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "runner")
public record RunnerProperties(
        Service service,
        Jwt jwt,
        Load load
) {
    public record Service(String baseUrl, boolean skipSslVerify) {}

    public record Jwt(String tokenUri, String clientId, String clientSecret, String grantType) {}

    // Only required under load profile
    public record Load(int fileSizeKb, int concurrency, int iterations, int warmup) {}
}
