package com.example.encryption.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import java.time.Duration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Bean
    public VaultTemplate vaultTemplate(VaultProperties vaultProperties) {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultProperties.url()));

        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(vaultProperties.roleId()))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(vaultProperties.secretId()))
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(vaultProperties.readTimeout());

        AppRoleAuthentication authentication = new AppRoleAuthentication(
                options, VaultClients.createRestTemplate(endpoint, requestFactory));

        return new VaultTemplate(endpoint, authentication);
    }
}
