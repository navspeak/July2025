package com.example.encryption.runner;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
@Profile("runner | load")
public class TokenProvider {

    private final RunnerProperties.Jwt props;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant expiresAt = Instant.EPOCH;

    public TokenProvider(RunnerProperties runnerProperties) {
        this.props = runnerProperties.jwt();
    }

    public synchronized String bearerToken() {
        if (cachedToken == null || Instant.now().isAfter(expiresAt.minusSeconds(10))) {
            fetch();
        }
        return "Bearer " + cachedToken;
    }

    @SuppressWarnings("unchecked")
    private void fetch() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", props.grantType());
        form.add("client_id", props.clientId());
        form.add("client_secret", props.clientSecret());

        Map<String, Object> response = restTemplate.postForObject(props.tokenUri(), form, Map.class);
        if (response == null || !response.containsKey("access_token")) {
            throw new IllegalStateException("Token endpoint did not return access_token");
        }
        cachedToken = (String) response.get("access_token");
        int expiresIn = ((Number) response.getOrDefault("expires_in", 60)).intValue();
        expiresAt = Instant.now().plusSeconds(expiresIn);
    }
}
