package com.example.encryption.runner;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("runner | load")
@Configuration
@EnableConfigurationProperties(RunnerProperties.class)
public class RunnerConfig {}
