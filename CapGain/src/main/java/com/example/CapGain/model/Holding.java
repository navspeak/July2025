package com.example.CapGain.model;

import java.math.BigDecimal;

public record Holding(
        Integer quantity,
        BigDecimal weightedAvgPrice,
        BigDecimal accruedLoss
){
    public static Holding empty(){ return new Holding(0, BigDecimal.ZERO, BigDecimal.ZERO);}
    public BigDecimal totalCostPrice() {
        return weightedAvgPrice.multiply(BigDecimal.valueOf(quantity));
    }
    public BigDecimal totalCostPriceFor(Integer qty) {
        return weightedAvgPrice.multiply(BigDecimal.valueOf(qty));
    }
}
