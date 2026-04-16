package com.example.practiceApps.domain;

import java.math.BigDecimal;

public record TradeResult(
        String tradeId,
        BigDecimal tax) {
}
