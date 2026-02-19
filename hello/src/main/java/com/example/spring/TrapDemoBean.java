package com.example.spring;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
class DatabaseService {
    public String getStatus() {
        return "Database is Connected!";
    }
}

@Component
public class TrapDemoBean {

    @Autowired
    private DatabaseService dbService; // Spring injects this AFTER the constructor

    // 1. THE CONSTRUCTOR (The Trap)
    public TrapDemoBean() {
        System.out.println("--- Phase 1: Constructor ---");

        try {
            // This will throw a NullPointerException!
            // Spring hasn't performed Dependency Injection yet.
            System.out.println("Attempting to use dbService: " + dbService.getStatus());
        } catch (NullPointerException e) {
            System.out.println("TRAP TRIGGERED: dbService is still NULL during construction.");
        }
    }

    // 2. POST CONSTRUCT (The Solution)
    @PostConstruct
    public void init() {
        System.out.println("--- Phase 2: @PostConstruct ---");
        // This works perfectly.
        // Spring has finished injecting all @Autowired fields.
        System.out.println("Safe to use dbService: " + dbService.getStatus());
    }
}