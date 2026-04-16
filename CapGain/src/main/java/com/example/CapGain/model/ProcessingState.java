package com.example.CapGain.model;

import java.util.ArrayList;
import java.util.List;

public record ProcessingState(Holding holding, List<Tax> taxList){
    public static ProcessingState initial() {
        return new ProcessingState(Holding.empty(), new ArrayList<>());
    }
}
