package com.example.practiceApps.service;

import com.example.practiceApps.domain.Trade;
import com.example.practiceApps.domain.TradeResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TradeService {

    private final TradeProcessor tradeProcessor;

    public TradeService(TradeProcessor tradeProcessor) {
        this.tradeProcessor = tradeProcessor;
    }

    public List<TradeResult> processTrades(List<Trade> trades) {
        return trades.stream()
                .map(this::processTrade)
                .toList();

    }

    private TradeResult processTrade(Trade trade){

    }
}
