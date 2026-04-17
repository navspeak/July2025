package com.example.encryption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * Token acquisition flow:
 *   1. On first Vault operation, AppRoleAuthentication calls POST /v1/auth/approle/login
 *      with role_id and secret_id to obtain a client_token.
 *   2. LifecycleAwareSessionManager caches the token and monitors its TTL.
 *   3. Before the token expires, the session manager proactively renews it via
 *      POST /v1/auth/token/renew-self — no manual intervention needed.
 *   4. If the token is non-renewable (policy restriction), the session manager
 *      re-authenticates automatically by repeating step 1.
 *
 * Without LifecycleAwareSessionManager, the token is obtained once and never
 * renewed — any request after TTL expiry would fail with 403.
 */
@Configuration
public class VaultConfig extends AbstractVaultConfiguration {

    private final VaultProperties vaultProperties;
    private final TaskScheduler taskScheduler;

    public VaultConfig(VaultProperties vaultProperties, TaskScheduler taskScheduler) {
        this.vaultProperties = vaultProperties;
        this.taskScheduler = taskScheduler;
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

    private AppRoleAuthenticationOptions buildOptions() {
        return AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(vaultProperties.roleId()))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(vaultProperties.secretId()))
                .build();
    }

    private RestTemplate buildRestTemplate() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(vaultProperties.readTimeout());

        RestTemplate restTemplate = VaultClients.createRestTemplate(vaultEndpoint(), requestFactory);

        // Vault namespace is sent as X-Vault-Namespace header on every request.
        // Mount paths (transit, secret) are relative within the namespace.
        //
        // Effective URL resolution:
        //   app.vault.url        = https://vault.host:8200
        //   app.vault.namespace  = harness/myproj
        //   app.vault.mount-path = transit
        //
        //   Spring Vault sends:
        //     POST https://vault.host:8200/v1/transit/encrypt/mykey
        //     X-Vault-Namespace: harness/myproj
        //
        //   Vault resolves internally to: /v1/harness/myproj/transit/encrypt/mykey
        if (vaultProperties.namespace() != null) {
            restTemplate.getInterceptors().add((request, body, execution) -> {
                request.getHeaders().set("X-Vault-Namespace", vaultProperties.namespace());
                return execution.execute(request, body);
            });
        }

        return restTemplate;
    }
}
