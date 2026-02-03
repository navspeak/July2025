package com.example.multithreading.orderentry.controllers;
import com.example.multithreading.orderentry.domain.OrderRequest;
import com.example.multithreading.orderentry.service.FanoutService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final FanoutService fanoutService;

    public OrderController(FanoutService fanoutService) {
        this.fanoutService = fanoutService;
    }

    @PostMapping("/submit")
    public ResponseEntity<?> submit(@RequestBody OrderRequest order) {
        FanoutService.FanoutResult result = fanoutService.fanout(order);

        if (result.fullySuccessful()) {
            return ResponseEntity.ok(result.response());
        }

        // Fail fast for “partial failure” so ops/user never assumes enriched state.
        return ResponseEntity.status(502).body(result.response());
    }
}
