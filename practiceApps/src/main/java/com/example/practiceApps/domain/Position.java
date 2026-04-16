package com.example.practiceApps.domain;

import java.math.BigDecimal;

public record Position(
        String symbol,
        int quantity,
        BigDecimal avgCost,
        BigDecimal accmulatedLoss
) {
    public Position ofQuantity(int newQuantity){
        return new Position(
                symbol,
                newQuantity,
                avgCost,
                accmulatedLoss
        );
    }

    public Position ofAvgCost(BigDecimal newAvgCost){
        return new Position(
                symbol,
                quantity,
                newAvgCost,
                accmulatedLoss
        );
    }

    public Position ofAccumulatedLost(BigDecimal newAccumulatedCost ){
        return new Position(
                symbol,
                quantity,
                avgCost,
                newAccumulatedCost
        );
    }
}
