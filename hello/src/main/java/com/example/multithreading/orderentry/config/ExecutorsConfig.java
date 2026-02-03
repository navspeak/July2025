package com.example.multithreading.orderentry.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class ExecutorsConfig {

    @Bean(name = "fanoutExecutor")
    public Executor fanoutExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(16);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(200);     // bounded queue -> backpressure
        ex.setThreadNamePrefix("fanout-");
        ex.initialize();
        return ex;
    }

    @Bean(name = "postProcessExecutor")
    public Executor postProcessExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(4);
        ex.setQueueCapacity(500);
        ex.setThreadNamePrefix("post-");
        ex.initialize();
        return ex;
    }
}

