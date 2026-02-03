package com.example.multithreading.orderentry.domain;

import java.time.Instant;
import java.util.Map;

public record FanoutResponse(
        String orderId,
        Instant receivedAt,
        FanoutSummary summary,
        Map<String, TargetOutcome> outcomes
) {}

