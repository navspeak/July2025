package com.example.encryption.cli;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestClient;

@Configuration
@Profile("cli")
public class CliConfig {

    @Bean
    RestClient restClient(CliProperties props) {
        return RestClient.builder()
                .baseUrl(props.serviceUrl())
                .build();
    }
}
