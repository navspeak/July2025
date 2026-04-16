package com.example.practiceApps.controller;

import com.example.practiceApps.domain.Trade;
import com.example.practiceApps.domain.Trade;
import com.example.practiceApps.domain.TradeResult;
import com.example.practiceApps.service.TradeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/trade")
public class TradeController {

    private final TradeService tradeService;
    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    List<TradeResult> trade(@RequestBody List<Trade> trades){
        return tradeService.processTrades(trades);
    }

}
