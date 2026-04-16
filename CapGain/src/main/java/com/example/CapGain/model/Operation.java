package com.example.CapGain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Operation {
    @JsonProperty("buy")
    BUY,
    @JsonProperty("sell")
    SELL
}
