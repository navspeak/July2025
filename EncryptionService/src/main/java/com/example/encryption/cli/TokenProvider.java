package com.example.encryption.cli;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Slf4j
@Component
@Profile("cli")
public class TokenProvider {

    private static final int EXPIRY_BUFFER_SECONDS = 30;

    private final CliProperties.Jwt props;
    private final String tokenUri;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public TokenProvider(CliProperties cliProperties) {
        this.props = cliProperties.jwt();
        this.tokenUri = cliProperties.jwt().tokenUri();
        enforceHttps();
    }

    public synchronized String getToken() {
        if (cachedToken == null || Instant.now().isAfter(expiresAt)) {
            fetch();
        }
        return cachedToken;
    }

    public synchronized void invalidate() {
        expiresAt = Instant.EPOCH;
    }

    private void enforceHttps() {
        if (tokenUri == null || !tokenUri.startsWith("https://")) {
            throw new IllegalStateException(
                "cli.jwt.token-uri must use HTTPS to protect client credentials. " +
                "Configured value: " + tokenUri);
        }
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("expires_in") int expiresIn
    ) {}

    private void fetch() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials");
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());

        TokenResponse response;
        try {
            response = restTemplate.postForObject(tokenUri, form, TokenResponse.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalStateException(
                "Token endpoint rejected credentials (clientId=" + props.clientId()
                + "): HTTP " + e.getStatusCode(), e);
        }

        if (response == null || response.accessToken() == null) {
            throw new IllegalStateException(
                "Token endpoint returned no access_token for clientId=" + props.clientId());
        }

        cachedToken = response.accessToken();
        expiresAt = Instant.now().plusSeconds(response.expiresIn() - EXPIRY_BUFFER_SECONDS);
        log.debug("Token fetched, expires in {}s", response.expiresIn());
    }
}
