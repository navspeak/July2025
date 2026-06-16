package com.example.encryption.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(CliProperties.class)
public class EncryptionCliApplication {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "cli");
        SpringApplication app = new SpringApplication(EncryptionCliApplication.class);
        app.run(args);
    }
}
