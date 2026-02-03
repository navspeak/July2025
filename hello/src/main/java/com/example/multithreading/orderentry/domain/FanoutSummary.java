package com.example.multithreading.orderentry.domain;


public record FanoutSummary(
        int totalTargets,
        int succeeded,
        int failed,
        boolean fullySuccessful
) {}


