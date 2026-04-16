package com.example.practiceApps.repository;

import com.example.practiceApps.domain.Position;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PositionRepository {

    private final Map<String, Optional<Position>> positionMap;

    public PositionRepository(List<Position> holdings) {
        this.positionMap = new HashMap<>();
    }


    public Optional<Position> findPosition(String symbol){
        return positionMap.getOrDefault(symbol, Optional.empty());
    }

    public void addPosition(Position position){
        positionMap.put(position.symbol(), Optional.of(position));
    }
}
