package com.example.CapGain.service;

import com.example.CapGain.model.Holding1;
import com.example.CapGain.model.Tax;

import com.example.CapGain.model.Transaction;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TaxCalculatorService {

    public List<Tax> computeTax(List<Transaction> transactions){
        return new TaxCalculator()
                .computeTax(transactions)
                .taxList();
//        Holding1 holding = getEmptyHolding();
//        List<Tax1> taxes = new ArrayList<>();
//        for(var transaction: transactions){
//            if (transaction.operation().equals(BUY)){
//                var newWeightedAvg = computeWeightedAvg(transaction, holding);
//                holding.setQuantity(holding.getQuantity() + transaction.quantity());
//                holding.setWeightedAvgUnitPrice(newWeightedAvg);
//                taxes.add(Tax1.of(BigDecimal.ZERO));
//            } else if (transaction.operation().equals(SELL)){
//                //You can assume that no operation will sell more stocks than you currently have.
//                var costPrice = holding.getTotalHoldingPriceFor(transaction.quantity());
//                var sellTotal = transaction.getTotalTransactionCost();
//                boolean sellTotalGT20K = sellTotal.compareTo(BigDecimal.valueOf(20_000)) > 0;
//                var profit = sellTotal.subtract(costPrice);
//
//                if (sellTotalGT20K) {
//                    profit = adjustAgainstLoss(holding, profit);
//                }
//
//                if (profit.signum() < 0){
//                    holding.setAccrudedLoss(holding.getAccrudedLoss().add(profit.abs()));
//                }
//                holding.setQuantity(holding.getQuantity() - transaction.quantity());
//
//                if (sellTotalGT20K && profit.signum() > 0){
//                    var tax = profit.multiply(new BigDecimal("0.20"));
//                    taxes.add(Tax1.of(tax));
//                } else {
//                    taxes.add(Tax1.of(BigDecimal.ZERO));
//                }
//            } else {
//                log.error("Transaction type not supported.");
//                throw new UnsupportedOperationException("Transaction type not supported.");
//            }
//        }
//        return taxes;
    }

//    private static BigDecimal adjustAgainstLoss(Holding1 holding, BigDecimal profit) {
//        var profitAdjustment = holding.getAccrudedLoss().min(profit);
//        profit = profit.subtract(profitAdjustment);
//        holding.setAccrudedLoss(
//                holding.getAccrudedLoss().subtract(profitAdjustment));
//        return profit;
//    }

//    private static BigDecimal computeWeightedAvg(Transaction_ transaction, Holding1 holding) {
//        Integer totalQty = holding.getQuantity() + transaction.quantity();
//        return holding.getTotalHoldingPrice()
//                .add(transaction.getTotalTransactionCost())
//                .divide(BigDecimal.valueOf(totalQty), 2, RoundingMode.HALF_UP);
//    }
//
//    private static Holding1 getEmptyHolding() {
//        return Holding1
//                .builder()
//                .quantity(0)
//                .weightedAvgUnitPrice(BigDecimal.ZERO)
//                .accrudedLoss(BigDecimal.ZERO)
//                .build();
//    }

}
