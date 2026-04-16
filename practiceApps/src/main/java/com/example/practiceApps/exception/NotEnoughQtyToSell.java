package com.example.practiceApps.exception;

public class NotEnoughQtyToSell extends RuntimeException {
    public NotEnoughQtyToSell(String message) {
        super(message);
    }
}
