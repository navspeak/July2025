package com.navn.securitydemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/api/stream").permitAll();
                    auth.anyRequest().authenticated();
                })
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf-> csrf.disable())
//              .formLogin(Customizer.withDefaults())
//              .oauth2Login(Customizer.withDefaults())


                .build();
// NOTE: In older spring we used  .oauth2AuthorizationServer(OAuth2ResourceServerConfigurer::jwt)

    }



    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("john") .password("{noop}password") // {noop} means no encoding
         .roles("USER").build();
        return new InMemoryUserDetailsManager(user);
    }

//    @Bean
//    public UserDetailsService jdbcuserDetailsService(DataSource dataSource) {
//        /* Spring Security provides a ready‑made schema: users table & authorities table */
//        /* Spring Security will automatically:
//          1. load users from DB 2. load roles/authorities 3.validate passwords
//        You must use the default schema unless you customize it Otherwise use CustomUserDetailsService */
//        return new JdbcUserDetailsManager(dataSource);
//    }

//    @Bean
//    public LdapContextSource contextSource() {
//        LdapContextSource source = new LdapContextSource();
//        source.setUrl("ldap://localhost:8389/");
//        source.setBase("dc=springframework,dc=org");
//        return source;
//    }
//
//    @Bean
//    public LdapUserDetailsService ldapUserDetailsService() {
//        return new LdapUserDetailsService("ou=people", contextSource());
//    }
//
//    spring.ldap.urls: ldap://localhost:8389
//    base.dc=springframework,dc=org


}
//jwt - 1: https://www.youtube.com/watch?v=KYNR5js2cXE
// jwt - 2:  https://www.youtube.com/watch?v=UaB-0e76LdQ

/*
CSRF:
  Invalid CSRF token found for http://localhost:8080/secured for basic
  CSRF does not block GET, HEAD, OPTION. CSRF does block POST, PUT, POST, PATCH, DELETE.
  Basic Auth does not bypass CSRF
1. curl -c cookies.txt -v http://localhost:8080/secured
      Response: Set-Cookie: JSESSIONID=abc123 and X-CSRF-TOKEN: xyz789
2. curl -b cookies.txt \
  -H "X-CSRF-TOKEN: xyz789" \
  -H "Content-Type: application/json" \
  -X POST http://localhost:8080/secured \
  -d '{"key":"hello","value":"world"}'


 */