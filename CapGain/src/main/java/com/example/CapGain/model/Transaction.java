package com.example.CapGain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Transaction (Operation operation,
                    @JsonProperty("unit-cost") BigDecimal unitCost,
                    Integer quantity){
    public BigDecimal totalSellPrice() {
        return unitCost.multiply(BigDecimal.valueOf(quantity));
    }
}
