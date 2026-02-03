package com.example.multithreading.orderentry.downstream;

import com.example.multithreading.orderentry.domain.OrderRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Component
public class DownstreamClient {

    private final WebClient webClient;

    public DownstreamClient(WebClient.Builder builder) {
        // Per-call timeout is best done at the HTTP client level.
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(2)); // response timeout

        this.webClient = builder
                .clientConnector(new org.springframework.http.client.reactive.ReactorClientHttpConnector(httpClient))
                .build();
    }

    public WebClient.ResponseSpec post(
            String url,
            String idempotencyKey,
            OrderRequest payload
    ) {
        return webClient.post()
                .uri(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("Idempotency-Key", idempotencyKey) // also send to target
                .bodyValue(payload)
                .retrieve();
    }
}
