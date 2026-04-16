package com.example.practiceApps.domain;

import com.example.practiceApps.domain.enums.TradeType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Trade(
        String tradeId,
        LocalDate date,
        String symbol,
        int quantity,
        TradeType type,
        BigDecimal unitPrice){

}
/*
{ "tradeId": "t1", "date": "2026-03-01", "symbol": "AAPL", "type": "BUY",  "quantity": 100, "unitPrice": 10.00 },
 */