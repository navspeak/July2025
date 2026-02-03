package com.example.multithreading.orderentry.service;

import com.example.multithreading.orderentry.domain.*;
import com.example.multithreading.orderentry.downstream.DownstreamClient;
import com.example.multithreading.orderentry.downstream.DownstreamTarget;
import com.example.multithreading.orderentry.repository.IdempotencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class FanoutService {

    private static final Logger log = LoggerFactory.getLogger(FanoutService.class);

    // Bounded concurrency at request level
    private static final int MAX_IN_FLIGHT = 4;

    private final Executor fanoutExecutor;
    private final Executor postProcessExecutor;
    private final DownstreamClient client;
    private final IdempotencyRepository idempotency;

    // In real code, externalize per-target URLs in config
    private final Map<DownstreamTarget, String> targetUrls = Map.of(
            DownstreamTarget.RISK, "http://risk/api/enrich",
            DownstreamTarget.COMPLIANCE, "http://compliance/api/enrich",
            DownstreamTarget.ALLOCATIONS, "http://allocations/api/enrich",
            DownstreamTarget.CONFIRMATIONS, "http://confirmations/api/enrich",
            DownstreamTarget.AUDIT, "http://audit/api/enrich",
            DownstreamTarget.LIMITS, "http://limits/api/enrich",
            DownstreamTarget.FEES, "http://fees/api/enrich",
            DownstreamTarget.REPORTING, "http://reporting/api/enrich"
    );

    public FanoutService(
            @Qualifier("fanoutExecutor")
            Executor fanoutExecutor,
            @Qualifier("postProcessExecutor")
            Executor postProcessExecutor,
            DownstreamClient client,
            IdempotencyRepository idempotency) {
        this.fanoutExecutor = fanoutExecutor;
        this.postProcessExecutor = postProcessExecutor;
        this.client = client;
        this.idempotency = idempotency;
    }

    public FanoutResult fanout(OrderRequest order) {
        Instant receivedAt = Instant.now();
        Semaphore bulkhead = new Semaphore(MAX_IN_FLIGHT);

        EnumMap<DownstreamTarget, CompletableFuture<TargetOutcome>> futures = new EnumMap<>(DownstreamTarget.class);

        for (DownstreamTarget target : DownstreamTarget.values()) {
            futures.put(target, CompletableFuture.supplyAsync(() -> {
                acquire(bulkhead);
                try {
                    return callTarget(order, target);
                } finally {
                    bulkhead.release();
                }
            }, fanoutExecutor));
        }

        // Wait for all to finish and aggregate (no swallowing)
        CompletableFuture<Void> all = CompletableFuture.allOf(
                futures.values().toArray(new CompletableFuture[0])
        );

        try {
            // hard cap so caller doesn't hang forever
            all.get(3, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            log.warn("Fanout timed out for orderId={}", order.orderId(), te);
            // Mark unfinished as failures
        } catch (Exception e) {
            log.error("Fanout aggregation failed orderId={}", order.orderId(), e);
        }

        // Collect outcomes
        Map<String, TargetOutcome> outcomes = new java.util.LinkedHashMap<>();
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (var entry : futures.entrySet()) {
            DownstreamTarget target = entry.getKey();
            TargetOutcome outcome = entry.getValue().handle((val, ex) -> {
                if (ex != null) {
                    return new TargetOutcome(
                            target.name(),
                            false,
                            0,
                            "EXCEPTION: " + rootMessage(ex),
                            0
                    );
                }
                return val;
            }).join();

            outcomes.put(target.name(), outcome);
            if (outcome.success()) ok.incrementAndGet(); else fail.incrementAndGet();
        }

        FanoutResponse response = new FanoutResponse(
                order.orderId(),
                receivedAt,
                new FanoutSummary(
                        DownstreamTarget.values().length,
                        ok.get(),
                        fail.get(),
                        fail.get() == 0
                ),
                outcomes
        );

        // Async post-processing (alerts/audit) should not block the HTTP response
        CompletableFuture.runAsync(() -> postProcess(order, response), postProcessExecutor)
                .exceptionally(ex -> {
                    log.error("Post-process failed orderId={}", order.orderId(), ex);
                    return null;
                });

        boolean fullyOk = response.summary().fullySuccessful();
        return new FanoutResult(fullyOk, response);
    }

    private TargetOutcome callTarget(OrderRequest order, DownstreamTarget target) {
        String idempotencyKey = order.orderId() + ":" + target.name();

        // Ensure our side is idempotent per (order, target)
        boolean firstTime = idempotency.tryInsert(target.name(), idempotencyKey);
        if (!firstTime) {
            return new TargetOutcome(target.name(), true, 200, "DUPLICATE-SKIPPED", 0);
        }

        String url = targetUrls.get(target);
        long start = System.nanoTime();

        try {
            // Turn reactive call into a bounded blocking call (MVC thread safe with timeouts)
            client.post(url, idempotencyKey, order)
                    .toBodilessEntity()
                    .timeout(java.time.Duration.ofSeconds(2))
                    .block();

            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new TargetOutcome(target.name(), true, 200, "OK", ms);

        } catch (WebClientResponseException wce) {
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new TargetOutcome(target.name(), false, wce.getRawStatusCode(), "HTTP " + wce.getStatusText(), ms);

        } catch (Exception e) {
            long ms = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return new TargetOutcome(target.name(), false, 0, "ERROR: " + rootMessage(e), ms);
        }
    }

    private void postProcess(OrderRequest order, FanoutResponse response) {
        if (!response.summary().fullySuccessful()) {
            // You’d typically publish a Kafka event / pager duty / ops alert here
            log.warn("PARTIAL FAILURE orderId={} succeeded={} failed={} outcomes={}",
                    order.orderId(),
                    response.summary().succeeded(),
                    response.summary().failed(),
                    response.outcomes()
            );
        } else {
            log.info("Fanout success orderId={} pTargets={}", order.orderId(), response.summary().totalTargets());
        }
    }

    private static void acquire(Semaphore s) {
        try {
            s.acquire();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted acquiring bulkhead", ie);
        }
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return (cur.getMessage() != null) ? cur.getMessage() : cur.getClass().getSimpleName();
    }

    public record FanoutResult(boolean fullySuccessful, FanoutResponse response) {}
}
