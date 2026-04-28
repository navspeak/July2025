# Observability Stack

## Components

| Tool | Purpose | URL |
|---|---|---|
| Prometheus | Scrapes `/actuator/prometheus` every 15s, stores time-series metrics | http://localhost:9090 |
| Grafana | Queries Prometheus, visualizes dashboards | http://localhost:3000 (admin/admin) |
| Zipkin | Receives and visualizes distributed traces | http://localhost:9411 |

## Startup

```bash
sh start.sh
```

Then start the encryption service — it will automatically ship metrics to Prometheus and traces to Zipkin.

---

## Metrics (Prometheus / Grafana)

The encryption service exposes metrics at `/actuator/prometheus`. Prometheus scrapes this endpoint every 15s.

### Useful Grafana queries (p99)

Metrics are published as pre-computed quantiles (not histogram buckets), so query directly by `quantile` label:

```promql
# End-to-end request latency
http_server_requests_seconds{quantile="0.99", uri="/api/v1/file/encrypt"}
http_server_requests_seconds{quantile="0.99", uri="/api/v1/file/decrypt"}

# Vault round-trip
vault_ops_latency_seconds{quantile="0.99"}

# File I/O — staging input to disk
file_stage_latency_seconds{quantile="0.99", operation="encrypt"}

# AES cipher stream
file_cipher_latency_seconds{quantile="0.99", operation="encrypt"}

# Streaming response back to client
file_stream_latency_seconds{quantile="0.99", operation="encrypt"}
```

These four (`stage`, `vault`, `cipher`, `stream`) should sum to roughly the end-to-end latency — compare them to identify the bottleneck.

> Note: values decay to 0 when no requests are flowing (sliding window). Query during or immediately after a benchmark run.

---

## Distributed Tracing (Zipkin)

### How it works

Each service propagates trace context in HTTP headers (`traceparent` / `X-B3-*`):

```
Service A  →  Service B  →  Service C  →  Service D
   |              |              |              |
creates        extracts       extracts       extracts
traceId=abc    traceId=abc    traceId=abc    traceId=abc
spanId=1       spanId=2       spanId=3       spanId=4
               parentId=1     parentId=2     parentId=3
```

1. Service A creates a new trace — generates `traceId` and its own `spanId`
2. When A calls B, it injects the trace context into the outgoing HTTP headers
3. Service B extracts the headers, creates a new span with the same `traceId`, new `spanId`, sets `parentId = A's spanId`
4. Each service independently ships its span to Zipkin
5. Zipkin reassembles the full trace by joining on `traceId`

### What Zipkin shows

```
traceId: abc123
├── A: /api/call          0ms ──────────────────── 335ms
│   ├── B: /encrypt        5ms ────────── 200ms
│   │   ├── C: /vault      10ms ──── 80ms
│   │   └── D: /storage    90ms ── 50ms
```

### In this service

`micrometer-tracing-bridge-brave` instruments `RestTemplate`, `WebClient`, and `RestClient` automatically — no manual header propagation needed. Spans are shipped to Zipkin via `zipkin-reporter-brave`.
