package com.example.encryption.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Bean
    public VaultTemplate vaultTemplate(
            @Value("${spring.vault.uri:http://localhost:8200}") String vaultUri,
            @Value("${spring.vault.app-role.role-id}") String roleId,
            @Value("${spring.vault.app-role.secret-id}") String secretId) {

        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(vaultUri));

        AppRoleAuthenticationOptions options = AppRoleAuthenticationOptions.builder()
                .roleId(AppRoleAuthenticationOptions.RoleId.provided(roleId))
                .secretId(AppRoleAuthenticationOptions.SecretId.provided(secretId))
                .build();

        AppRoleAuthentication authentication = new AppRoleAuthentication(
                options, endpoint.createRestTemplate());

        return new VaultTemplate(endpoint, authentication);
    }
}
