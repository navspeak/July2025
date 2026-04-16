package com.example.CapGain.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Holding1 {
    private Integer quantity;
    private BigDecimal weightedAvgUnitPrice;
    private BigDecimal accrudedLoss;

    public BigDecimal getTotalHoldingPrice(){
        return getWeightedAvgUnitPrice()
                .multiply(BigDecimal.valueOf(getQuantity()));
    }

    public BigDecimal getTotalHoldingPriceFor(Integer qty){
        return weightedAvgUnitPrice.multiply(BigDecimal.valueOf(qty));
    }
}
