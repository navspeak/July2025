package com.example.CapGain.model;

import java.math.BigDecimal;

public record Holding__(
        Integer quantity,
        BigDecimal wtAvgCost,
        BigDecimal accruedLoss
) {
    public static Holding__ of(Integer qty, BigDecimal wtAvgCost, BigDecimal accruedLoss ){
        return new Holding__(qty, wtAvgCost, accruedLoss);
    }
    public BigDecimal getTotalHoldingPriceFor(Integer qty){
        return wtAvgCost.multiply(BigDecimal.valueOf(qty));
    }
}
