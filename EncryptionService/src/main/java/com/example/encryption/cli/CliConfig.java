package com.example.encryption.cli;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.http.HttpClient;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
@Profile("cli")
public class CliConfig {

    @Bean
    RestClient restClient(CliProperties props) throws Exception {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(props.serviceUrl());

        if (props.skipSslVerify()) {
            builder.requestFactory(new JdkClientHttpRequestFactory(trustAllHttpClient()));
        }

        return builder.build();
    }

    private HttpClient trustAllHttpClient() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {}
            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
        }}, new SecureRandom());

        return HttpClient.newBuilder().sslContext(sslContext).build();
    }
}
