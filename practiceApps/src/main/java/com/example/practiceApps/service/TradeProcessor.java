package com.example.practiceApps.service;

import com.example.practiceApps.domain.Position;
import com.example.practiceApps.domain.Trade;
import com.example.practiceApps.domain.enums.TradeType;
import com.example.practiceApps.exception.NotEnoughQtyToSell;
import com.example.practiceApps.exception.PositionNotFound;
import com.example.practiceApps.repository.PositionRepository;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
public class TradeProcessor {

    private final BigDecimal TAX = new BigDecimal("0.2") ;
    private final PositionRepository positionRepository;
    public TradeProcessor(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }
    // Process trade and return tax Due
    public BigDecimal processTrade(Trade trade){
        return switch(trade.type()){
            case BUY -> processBuy(trade);
            case SELL -> processSell(trade);
        };

    }

    // Process Sell and returns tax due
    private BigDecimal processSell(Trade trade) {
        String symbol = trade.symbol();
        Optional<Position> positionOptional = positionRepository.findPosition(symbol);
        if (!positionOptional.isPresent()) {
            log.warn("No position held for "+ trade.symbol());
            throw new PositionNotFound("No position held for "+ trade.symbol());
        }
        Position position = positionOptional.get();
        BigDecimal tradeQty = BigDecimal.valueOf(trade.quantity());
        if (trade.quantity() > position.quantity()){
            log.info("Requested Trade quality {} is greater than available {}", trade.quantity() , position.quantity());
            throw new NotEnoughQtyToSell(trade.quantity() + "exceeds the quantity held");
        }
        BigDecimal acc = position.accmulatedLoss();
        var proceeds = trade.unitPrice()
                            .multiply(tradeQty)
                            .setScale(2, RoundingMode.HALF_UP);
        var costBasis = position.avgCost()
                            .multiply(tradeQty)
                            .setScale(2, RoundingMode.HALF_UP);
        var pnl = proceeds.subtract(costBasis);
        positionRepository.addPosition(
                position.ofQuantity(position.quantity()
                        - trade.quantity()));

        if (pnl.compareTo(BigDecimal.ZERO) > 0){
            return TAX.multiply(pnl).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;

    }

    // Process Buy and returns tax due
    private BigDecimal processBuy(Trade trade) {
        String symbol = trade.symbol();
        var positionOptional = positionRepository.findPosition(trade.symbol());
        BigDecimal newTotalPrice = trade.unitPrice().multiply(
                BigDecimal.valueOf(trade.quantity())
        ) ;
        int totalQuantity = trade.quantity();
        BigDecimal accumulatedLoss = BigDecimal.ZERO;
        if (positionOptional.isPresent()) {
            var position = positionOptional.get();
            var existingTotalPrice = position.avgCost().multiply(
                    BigDecimal.valueOf(position.quantity()));
            newTotalPrice = newTotalPrice.add(existingTotalPrice);
            totalQuantity += trade.quantity();
            accumulatedLoss = position.accmulatedLoss();
        }
        BigDecimal newAvgCost = newTotalPrice.divide(BigDecimal.valueOf(totalQuantity),
                2, RoundingMode.HALF_UP);
        positionRepository.addPosition(new Position(
                symbol,
                totalQuantity,
                newAvgCost,
                accumulatedLoss
        ));
        return BigDecimal.ZERO;
    }
}
