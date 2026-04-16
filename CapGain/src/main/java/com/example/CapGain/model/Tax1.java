package com.example.CapGain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Tax1(
        BigDecimal tax
) {
    public static Tax1 of(BigDecimal val){
        return new Tax1(val.setScale(1, RoundingMode.HALF_UP));
    }

//    public String toString(){
//        return """
//                {"tax": %s}\
//                """.formatted(tax.toPlainString());
//    }
}
