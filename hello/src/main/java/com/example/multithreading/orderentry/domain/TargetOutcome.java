package com.example.multithreading.orderentry.domain;

public record TargetOutcome(
        String target,
        boolean success,
        int httpStatus,
        String message,
        long latencyMs
) {}