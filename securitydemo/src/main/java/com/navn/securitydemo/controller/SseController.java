package com.navn.securitydemo.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class SseController {

    // Keep track of connected clients
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseController() {
        // Optional: send a heartbeat to all clients every 10 seconds
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            broadcast("heartbeat", "ts=" + Instant.now());
        }, 0, 10, TimeUnit.SECONDS);
    }

    // Client subscribes here: GET /api/stream
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        // 0L = no timeout (you can set e.g. 30_000L)
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        // Remove emitter on lifecycle events
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((ex) -> emitters.remove(emitter));

        // Send an initial event so client knows it's connected
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("Connected at " + Instant.now()));
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        return emitter;
    }

    // Example: trigger a broadcast manually
    // POST /api/publish?msg=hello
    @PostMapping("/publish")
    public ResponseEntity<String> publish(@RequestParam String msg) {
        broadcast("message", msg);
        return ResponseEntity.ok("sent");
    }

    private void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception ex) {
                emitters.remove(emitter);
            }
        }
    }
}
/*
C:\Users\navne>curl -N http://localhost:8080/api/stream
event:connected
data:Connected at 2026-02-26T20:23:33.130064600Z

event:heartbeat
data:ts=2026-02-26T20:23:35.613658400Z

event:heartbeat
data:ts=2026-02-26T20:23:45.416872100Z

event:heartbeat
data:ts=2026-02-26T20:24:02.139162Z

event:heartbeat
data:ts=2026-02-26T20:24:05.422026100Z
 */