package com.example.CapGain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record Transaction_(
        Operation operation,
        @JsonProperty("unit-cost")
        BigDecimal unitCost,
        Integer quantity
) {
        public BigDecimal getTotalTransactionCost(){
                return unitCost.multiply(BigDecimal.valueOf(quantity));
        }

}
