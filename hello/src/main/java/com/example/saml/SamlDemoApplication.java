package com.example.saml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml2.provider.service.registration.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
public class SamlDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamlDemoApplication.class, args);
    }

    // ============================================================
    // 1. Security Configuration (SAML SP)
    // ============================================================
    @Configuration
    static class SamlSecurityConfig {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

            http.authorizeHttpRequests(auth -> auth
                            .requestMatchers("/", "/public").permitAll()
                            .anyRequest().authenticated()
                    )
                    // 🔥 New 6.1+ style with simple customizer
                    .saml2Login(saml -> saml
                            .defaultSuccessUrl("/secure") // after successful login
                            .loginPage("/custom-login")   // optional custom login page
                    );

            return http.build();
        }

        // ============================================================
        // 2. SAML Registration Bean — your SP configuration
        // ============================================================
        @Bean
        RelyingPartyRegistrationRepository rpRepository() throws Exception {

            // Load IdP metadata as InputStream
            ClassPathResource idpMetadata = new ClassPathResource("idp-metadata.xml");

            RelyingPartyRegistration registration =
                    RelyingPartyRegistrations
                            .fromMetadata(idpMetadata.getInputStream()) // ✅ InputStream
                            .registrationId("my-saml-idp")
                            .entityId("urn:example:my-sp")
                            .build();

            return new InMemoryRelyingPartyRegistrationRepository(registration);
        }
    }

    // ============================================================
    // 3. Demo Controller
    // ============================================================
    @RestController
    static class DemoController {

        @GetMapping("/")
        public String home() {
            return "Public Home Page";
        }

        @GetMapping("/secure")
        public String secure(org.springframework.security.core.Authentication auth) {
            return "You are logged in via SAML as: " + auth.getName();
        }

        @GetMapping("/custom-login")
        public String customLogin() {
            return "<h2>Custom SAML Login Page</h2>" +
                    "<p>This is a placeholder login page before redirecting to IdP.</p>";
        }
    }
}
