package com.example.multithreading.orderentry.domain;

import java.util.Map;

public record OrderRequest(
        String orderId,
        String accountId,
        String symbol,
        int quantity,
        double price,
        Map<String, Object> attributes
) {}