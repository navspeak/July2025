package com.example.practiceApps.exception;

public class PositionNotFound extends RuntimeException{
    public PositionNotFound(String msg) {
        super(msg);
    }
}
