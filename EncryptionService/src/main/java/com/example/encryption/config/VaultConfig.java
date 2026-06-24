package com.example.encryption.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.ClientAuthentication;
import org.springframework.vault.authentication.LifecycleAwareSessionManager;
import org.springframework.vault.authentication.SessionManager;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.config.AbstractVaultConfiguration;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import javax.net.ssl.SSLContext;

/**
 * Token acquisition flow:
 *   1. On first Vault operation, AppRoleAuthentication calls POST /v1/auth/<authMountPath>/login
 *      with role_id and secret_id to obtain a client_token.
 *   2. LifecycleAwareSessionManager caches the token and monitors its TTL.
 *   3. Before the token expires, the session manager proactively renews it via
 *      POST /v1/auth/token/renew-self — no manual intervention needed.
 *   4. If the token is non-renewable (policy restriction), the session manager
 *      re-authenticates automatically by repeating step 1.
 *
 * Without LifecycleAwareSessionManager, the token is obtained once and never
 * renewed — any request after TTL expiry would fail with 403.
 *
 * URL resolution:
 *   harness/myproj is part of the mount path, not a Vault namespace.
 *   All mount paths must include the full prefix, e.g.:
 *     transit-mount-path: harness/myproj/transit
 *     kv-mount-path:      harness/myproj/data
 *
 *   Spring Vault sends:
 *     POST https://vault.host:8200/v1/harness/myproj/transit/encrypt/my-key
 *     GET  https://vault.host:8200/v1/harness/myproj/data/data/{path}
 *
 * Vault round-trip metrics:
 *   Recorded as Micrometer timer: vault.ops.latency (tags: method, status)
 *   Query via actuator: GET /actuator/metrics/vault.ops.latency
 *   Example response shows count, mean, max across all Vault HTTP calls.
 */
@Slf4j
@Configuration
@Profile("!cli")
public class VaultConfig extends AbstractVaultConfiguration {

    private final VaultProperties vaultProperties;
    private final TaskScheduler taskScheduler;
    private final MeterRegistry meterRegistry;

    public VaultConfig(VaultProperties vaultProperties, TaskScheduler taskScheduler, MeterRegistry meterRegistry) {
        this.vaultProperties = vaultProperties;
        this.taskScheduler = taskScheduler;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public VaultEndpoint vaultEndpoint() {
        return VaultEndpoint.from(URI.create(vaultProperties.url()));
    }

    @Override
    public ClientAuthentication clientAuthentication() {
        return new AppRoleAuthentication(buildOptions(), buildRestTemplate());
    }

    @Bean
    @Override
    public SessionManager sessionManager() {
        return new LifecycleAwareSessionManager(clientAuthentication(), taskScheduler, buildRestTemplate());
    }

    @Bean
    @Override
    public VaultTemplate vaultTemplate() {
        JdkClientHttpRequestFactory base = createSecureRequestFactory(vaultProperties.readTimeout());

        var factory = new InterceptingClientHttpRequestFactory(base, List.of(
                (request, body, execution) -> {
                    log.debug("Vault ops: {} {}", request.getMethod(), request.getURI());
                    Timer.Sample sample = Timer.start(meterRegistry);
                    var response = execution.execute(request, body);
                    sample.stop(Timer.builder("vault.ops.latency")
                            .tag("method", request.getMethod().name())
                            .tag("status", String.valueOf(response.getStatusCode().value()))
                            .register(meterRegistry));
                    log.debug("Vault ops response: {}", response.getStatusCode());
                    return response;
                }
        ));

        return new VaultTemplate(() -> vaultEndpoint(), factory, sessionManager());
    }

    private AppRoleAuthenticationOptions buildOptions() {
        return AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(vaultProperties.roleId()))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(vaultProperties.secretId()))
                // Full mount path in URL: POST /v1/auth/<authMountPath>/login
                .path(vaultProperties.authMountPath())
                .build();
    }

    // Auth only — plain RestTemplate, no interceptors.
    private RestTemplate buildRestTemplate() {
        return VaultClients.createRestTemplate(vaultEndpoint(), createSecureRequestFactory(vaultProperties.readTimeout()));
    }

    // Pins TLS randomness to SecureRandom, preventing weak PRNG reachability through JDK HttpClient's JCA init path.
    private static JdkClientHttpRequestFactory createSecureRequestFactory(Duration readTimeout) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, new SecureRandom());
            JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(
                    HttpClient.newBuilder().sslContext(sslContext).build());
            factory.setReadTimeout(readTimeout);
            return factory;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Failed to initialize secure HTTP client for Vault", e);
        }
    }
}
