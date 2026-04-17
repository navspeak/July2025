package com.example.encryption;

import com.example.encryption.config.VaultProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(VaultProperties.class)
public class EncryptionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EncryptionServiceApplication.class, args);
    }
}
