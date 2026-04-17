package com.example.encryption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(vaultProperties.readTimeout());

        RestTemplate restTemplate = VaultClients.createRestTemplate(vaultEndpoint(), requestFactory);

        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(vaultProperties.roleId()))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(vaultProperties.secretId()))
                .build();

        return new AppRoleAuthentication(options, restTemplate);
    }

    @Bean
    @Override
    public SessionManager sessionManager() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(vaultProperties.readTimeout());

        RestTemplate restTemplate = VaultClients.createRestTemplate(vaultEndpoint(), requestFactory);

        return new LifecycleAwareSessionManager(clientAuthentication(), taskScheduler, restTemplate);
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("vault-session-");
        scheduler.initialize();
        return scheduler;
    }
}
