package com.example.encryption.util;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!cli")
public class CipherBulkhead {

    @FunctionalInterface
    public interface CheckedRunnable {
        void run() throws Exception;
    }

    private static final long LARGE_FILE_THRESHOLD = 1024 * 1024;

    private final Bulkhead small;
    private final Bulkhead large;

    public CipherBulkhead(BulkheadRegistry registry) {
        this.small = registry.bulkhead("cipherOps");
        this.large = registry.bulkhead("cipherOpsLarge");
        registerEvents(small);
        registerEvents(large);
    }

    public void execute(long fileSize, CheckedRunnable action) throws Exception {
        Bulkhead bulkhead = fileSize > LARGE_FILE_THRESHOLD ? large : small;
        bulkhead.acquirePermission();
        try {
            action.run();
        } finally {
            bulkhead.releasePermission();
        }
    }

    private void registerEvents(Bulkhead bulkhead) {
        bulkhead.getEventPublisher()
            .onCallPermitted(e -> log.debug("[{}] call permitted — inFlight={}/{}",
                bulkhead.getName(), inFlight(bulkhead), bulkhead.getMetrics().getMaxAllowedConcurrentCalls()))
            .onCallRejected(e -> log.warn("[{}] call REJECTED — bulkhead full ({}/{})",
                bulkhead.getName(), inFlight(bulkhead), bulkhead.getMetrics().getMaxAllowedConcurrentCalls()))
            .onCallFinished(e -> log.debug("[{}] call finished — inFlight={}/{}",
                bulkhead.getName(), inFlight(bulkhead), bulkhead.getMetrics().getMaxAllowedConcurrentCalls()));
    }

    private int inFlight(Bulkhead bulkhead) {
        return bulkhead.getMetrics().getMaxAllowedConcurrentCalls()
             - bulkhead.getMetrics().getAvailableConcurrentCalls();
    }
}
