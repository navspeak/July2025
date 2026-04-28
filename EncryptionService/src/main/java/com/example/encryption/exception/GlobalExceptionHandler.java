package com.example.encryption.exception;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@Profile("!client")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "File size exceeds the maximum allowed limit of 1MB"));
    }

    // Resilience4j bulkhead is full — server is at max concurrent cipher capacity.
    // 429 tells the client to back off and retry; no point queuing on our side (maxWaitDuration=0ms).
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<Map<String, String>> handleBulkheadFull(BulkheadFullException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of("error", "Server is at capacity — too many concurrent requests, please retry"));
    }
}
