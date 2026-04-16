package com.example.CapGain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Tax (BigDecimal tax){
    public static Tax of(BigDecimal val){
        return new Tax(val.setScale(1, RoundingMode.HALF_UP));
    }
}