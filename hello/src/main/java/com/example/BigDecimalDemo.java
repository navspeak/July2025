package com.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.math.RoundingMode.*;

public class BigDecimalDemo {
    public static void main(String[] args) {
        System.out.println(new BigDecimal("10.29")); //best way
        System.out.println(new BigDecimal(10.299));
        System.out.println(BigDecimal.valueOf(10.299));
        var one = new BigDecimal("1");
        var two = new BigDecimal("2");
        var three = new BigDecimal("3");
        System.out.println(one.divide(two));
//        System.out.println(one.divide(three));
        System.out.println(one.divide(three, 3)); // Dangerous. 3rd param taken as rounding mode, not scale
        System.out.println(one.divide(three, 3, HALF_UP)); //0.3333 -> 0.333
        System.out.println(one.divide(three, 2, HALF_UP)); //0.3333 -> 0.333
        System.out.println("----");
        System.out.println(new BigDecimal("1.31").setScale(1, HALF_EVEN));
        System.out.println(new BigDecimal("1.32").setScale(1, HALF_EVEN));
        System.out.println(new BigDecimal("1.35").setScale(1, HALF_EVEN)); //1.4
        System.out.println(new BigDecimal("1.36").setScale(1, HALF_EVEN));
        System.out.println(new BigDecimal("1.44").setScale(1, HALF_EVEN));
        System.out.println(new BigDecimal("1.45").setScale(1, HALF_EVEN)); // 1.4
        System.out.println(new BigDecimal("1.46").setScale(1, HALF_EVEN));
        System.out.println(new BigDecimal("1.55").setScale(1, HALF_EVEN));
    }
}
