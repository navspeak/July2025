package com.example.CapGain.service;

import com.example.CapGain.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class TaxCalculator {

    public static BigDecimal THRESHOLD = new BigDecimal("20000");
    public static BigDecimal TAX_PCT = new BigDecimal("0.20");

    public ProcessingState computeTax(List<Transaction> transactions){
//       return transactions
//               .stream() //
//               .reduce(
//                       ProcessingState.initial(),
//                       (processingState, transaction) -> applyBuyOrSell(processingState, transaction),
//                       (ps1, ps2) -> combineProcessingState(ps1, ps2)
//               );

        // FIX: allocate ONE list here and pass it through the entire computation.
        // Previously, applyBuy/applySell each did new ArrayList<>(state.taxList()) per transaction,
        // causing O(N²) allocations — transaction 1 copies 0 elements, transaction 2 copies 1,
        // transaction 3 copies 2, ... transaction N copies N-1. Total = N*(N-1)/2 wasted copies.
        // Now the list grows in-place: O(N) total allocations, zero copies discarded.
        List<Tax> sharedTaxList = new ArrayList<>();
        Holding holding = Holding.empty();

        for (var t : transactions) {
            holding = applyBuyOrSell(holding, sharedTaxList, t);
        }
        return new ProcessingState(holding, sharedTaxList);
    }

    // FIX: signature changed — accepts Holding + sharedTaxList directly instead of ProcessingState.
    // applyBuy/applySell now append to sharedTaxList in-place and return only the new Holding.
    // ProcessingState is no longer created per transaction — only once at the end in computeTax.
    private Holding applyBuyOrSell(Holding holding, List<Tax> sharedTaxList, Transaction transaction) {
        return switch (transaction.operation()){
            case BUY -> applyBuy(holding, sharedTaxList, transaction);
            case SELL -> applySell(holding, sharedTaxList, transaction);
        };
    }

    private Holding applySell(Holding holding, List<Tax> sharedTaxList, Transaction transaction) {
        // ORIGINAL LOGIC UNCHANGED — only the list handling at the bottom is fixed
        var totalSellPrice = transaction.totalSellPrice();
        var totalCostPrice = holding.totalCostPriceFor(transaction.quantity());
        var rawProfit = totalSellPrice.subtract(totalCostPrice);
        var isSaleGreaterThanThreshold = (totalSellPrice.compareTo(THRESHOLD) > 0);
        var lossAdjustment = BigDecimal.ZERO;
        var tax = Tax.of(BigDecimal.ZERO);
        var accruredLoss = holding.accruedLoss();
        if (isSaleGreaterThanThreshold){
            lossAdjustment = rawProfit.min(holding.accruedLoss()).max(BigDecimal.ZERO);
            var taxableProfit = rawProfit.subtract(lossAdjustment);
            accruredLoss = accruredLoss.subtract(lossAdjustment);
            if (taxableProfit.signum() > 0){
                tax = Tax.of(taxableProfit.multiply(TAX_PCT));
            }
        }

        if (rawProfit.signum() < 0){
            /*
            when original profit was positive, adjusted profit cannot become negative
            when original profit was negative, adjustment is forced to zero

            Still for readbility we reduce rawProfit
             */
            accruredLoss = accruredLoss.add(rawProfit.abs());
        }

        // FIX: append to sharedTaxList in-place — no copy, no new list object created.
        // Previously: new ArrayList<>(state.taxList()) created a full copy here → discarded immediately.
        sharedTaxList.add(tax);

        var totalQuantity = holding.quantity() - transaction.quantity();
        // FIX: return only Holding — ProcessingState created once at the end, not per transaction.
        return new Holding(totalQuantity, holding.weightedAvgPrice(), accruredLoss);
    }

    private Holding applyBuy(Holding holding, List<Tax> sharedTaxList, Transaction transaction) {
        // ORIGINAL LOGIC UNCHANGED — only the list handling at the bottom is fixed
        int totalQuantity = holding.quantity() + transaction.quantity();
        var weightedAvgPrice = holding.weightedAvgPrice()
                .multiply(BigDecimal.valueOf(holding.quantity()))
                .add(transaction.unitCost().multiply(BigDecimal.valueOf(transaction.quantity())))
                .divide(BigDecimal.valueOf(totalQuantity), 1, RoundingMode.HALF_UP);

        // FIX: append to sharedTaxList in-place — no copy, no new list object created.
        // Previously: new ArrayList<>(processingState.taxList()) created a full copy here → discarded immediately.
        sharedTaxList.add(Tax.of(BigDecimal.ZERO));

        // FIX: return only Holding — ProcessingState created once at the end, not per transaction.
        return new Holding(totalQuantity, weightedAvgPrice, holding.accruedLoss());
    }


//    private ProcessingState combineProcessingState(ProcessingState ps1, ProcessingState ps2) {
//        List<Tax> combinedTax = new ArrayList<>();
//        combinedTax.addAll(ps1.taxList());
//        combinedTax.addAll(ps2.taxList());
//        return new ProcessingState(ps2.holding(), combinedTax );
//    }


}
