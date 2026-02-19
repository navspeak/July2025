package com.example.spring;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/*
Even with Constructor Injection, you still need @PostConstruct for any logic that shouldn't happen during the "birth" of
the object, but rather once the object is "ready for work."

A classic example is Warming up a Cache or Starting a Background Thread.
The constructor sets up the "what" (dependencies), and @PostConstruct sets up the "how" (initial state).
 */
@Component
public class CurrencyRateService {

    private final DatabaseRepository repository;
    private final Map<String, Double> rateCache = new ConcurrentHashMap<>();

    // 1. CONSTRUCTOR INJECTION
    // We get our dependencies here. The object is "born."
    public CurrencyRateService(DatabaseRepository repository) {
        this.repository = repository;
        System.out.println("Step 1: Constructor - Repository is now assigned.");
        // Note: We DON'T start heavy logic here because the bean
        // isn't fully managed by Spring's AOP/Proxies yet.
    }

    // 2. @POSTCONSTRUCT
    // This runs after the constructor AND after any BeanPostProcessors
    // have finished their 'before' phase.
    /*
    Why not just put loadCache() in the Constructor?
    There are three big reasons why you should keep "action" logic out of the constructor and in @PostConstruct:
    1. Proxies and AOP: If your repository.findAllRates() method is marked as @Transactional,
    it might not work correctly inside a constructor because the Spring Proxy (the "shell" that handles transactions)
    hasn't been wrapped around the bean yet.
    2. registration: The constructor's only job is to build the object.
    If loadCache() fails or takes 10 seconds, you are blocking the literal instantiation of the class.
    @PostConstruct is the formal "I am ready to start my engines" signal.
    3. Readability: It separates Dependency Wiring (Constructor) from Logic Initialization (@PostConstruct).
     */
    @PostConstruct
    public void loadCache() {
        System.out.println("Step 2: @PostConstruct - Loading data into cache...");

        // We use the injected repository to fill our map
        List<Rate> rates = repository.findAllRates();
        rates.forEach(r -> rateCache.put(r.getCurrency(), r.getValue()));

        System.out.println("Cache populated with " + rateCache.size() + " items.");
    }

    public Double getRate(String currency) {
        return rateCache.get(currency);
    }

    static class Rate {

        public String getCurrency() {
            return null;
        }

        public Double getValue() {
            return null;
        }
    }

    static class DatabaseRepository {

        public List<Rate> findAllRates() {
            return null;
        }
    }
}