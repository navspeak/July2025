package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MagicNumberRemoval {

//    @Autowired
    private ShippingProperties properties;
    //properties
    // shipping:
    //     slabs:
    //       - maxwt: 10
    //         cost
    //       - maxwt: 20
    //         cost
    //       - maxwt: 30
    //         cost

    static class ShippingSvc {
        Double calculatePrice(Double weight){
//            if (weight < 10) return 100.50;
//            if (weight < 20) return 110.50;
//            return 130.50;
            return ShippingSlab.costFor(weight);
        }

        Double calculatePrice_v2(Double weight){
//            if (weight < 10) return 100.50;
//            if (weight < 20) return 110.50;
//            return 130.50;
            return ShippingSlab.costFor(weight);
        }
    }

    enum ShippingSlab{
        LIGHT(10, 100.50),
        MEDIUM(20, 110.50),
        HEAVY(Double.MAX_VALUE, 130.50);
        private Double maxWt;
        private Double cost;

        ShippingSlab(double maxWt, double cost) {
            this.maxWt = maxWt;
            this.cost = cost;
        }

        public static Double costFor(Double weight){
            return Arrays.stream(values())
                    .filter(slab -> weight <= slab.maxWt)
                    .sorted(Comparator.comparing(slab -> slab.maxWt)) // not needed because
                    .findFirst()
                    .orElseThrow()
                    .cost;
        }
    }

    public static void main(String[] args) {
        ShippingSvc svc = new ShippingSvc();
        System.out.println(svc.calculatePrice(12.4));
        System.out.println(Arrays.toString(ShippingSlab.values()));
        System.out.println(ShippingSlab.valueOf("Heavy".toUpperCase()).cost);
        System.out.println(ShippingSlab.LIGHT.name());
        System.out.println(ShippingSlab.LIGHT.cost);
    }

    @ConfigurationProperties(prefix = "shipping")
    public static class ShippingProperties {
        private List<Slab> slabs;
        record Slab(double maxWt, double cost){}
    }
}
