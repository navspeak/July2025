# Observability Concepts

## Micrometer

Micrometer is a **metrics instrumentation library** for JVM applications — think of it as a facade (like SLF4J but for metrics).

You write metric code once using Micrometer's API, and it publishes to any backend (Prometheus, Datadog, CloudWatch, etc.) by swapping the registry dependency.

**What it provides:**
- `Counter` — counts events (requests, errors)
- `Timer` — measures duration + throughput (latency p50/p95/p99)
- `Gauge` — point-in-time value (active connections, queue size)
- `DistributionSummary` — distribution of values (file sizes)

**Example:**
```java
// Declare
Timer timer = Timer.builder("file.cipher.latency")
        .tag("operation", "encrypt")
        .register(meterRegistry);

// Use
timer.record(() -> doCipherWork());
```

Micrometer auto-instruments Spring Boot (HTTP requests, JVM heap, GC, thread pools) with no extra code.

---

## MDC (Mapped Diagnostic Context)

MDC is a **per-thread key-value store** built into SLF4J/Log4j2. You put context into it once, and every log line on that thread automatically includes it — no need to pass values through every method call.

**Typical use: inject traceId at request entry so all logs for that request carry it.**

```java
// In a filter (once per request)
MDC.put("traceId", span.traceId());
MDC.put("userId", jwt.getSubject());

// Anywhere in the call chain — no parameter passing needed
log.info("File staged");   // logs: traceId=abc123 userId=u99 File staged
log.warn("Vault timeout"); // logs: traceId=abc123 userId=u99 Vault timeout

// Always clear at end of request (thread pool reuse)
MDC.clear();
```

**In log4j2-spring.xml pattern:**
```xml
%X{traceId} %X{userId} [%m]%n
```

`%X{key}` pulls the MDC value into every log line automatically.

**Why it matters:** when debugging a failed request, you can grep logs by `traceId` and see the full story of that one request across all classes.

---

## Prometheus

Prometheus is a **time-series metrics database and scraper**.

- **Pull model:** Prometheus calls your app's `/actuator/prometheus` endpoint every N seconds and stores the snapshot
- Stores metrics as `metric_name{label="value"} numeric_value timestamp`
- Has its own query language: **PromQL**

```
# p95 latency of encrypt endpoint over last 5 minutes
histogram_quantile(0.95,
  rate(file_cipher_latency_seconds_bucket{operation="encrypt"}[5m])
)
```

Prometheus **does not visualize** — it only stores and queries. Grafana reads from it.

---

## Zipkin

Zipkin is a **distributed tracing system**.

When a request touches multiple services (or multiple layers in one service), Zipkin stitches them into a single **trace** — a tree of **spans** showing where time was spent.

```
Trace abc123
├── span: HTTP POST /api/v1/file/encrypt      120ms
│   ├── span: VaultConfig.getKey              45ms
│   ├── span: FileEncryptionService.cipher    60ms
│   └── span: stream response                 15ms
```

- Your app sends spans to Zipkin via OTLP (`localhost:4318`) or HTTP
- Zipkin UI lets you search by traceId, service, duration
- Answers: *which span is slow? where did this request fail?*

**Micrometer Tracing** auto-creates spans for HTTP requests, DB calls, etc. You can add custom spans manually.

---

## Kibana

Kibana is a **log search and visualization UI** for Elasticsearch (the ELK stack).

```
App → (logs) → Logstash/Filebeat → Elasticsearch → Kibana
```

- Elasticsearch indexes every log line as a searchable JSON document
- Kibana lets you search full-text, filter by field, build dashboards from logs
- Best for: *show me all ERROR logs for traceId=abc123 in the last hour*

**vs Prometheus/Grafana:** Prometheus is for *numeric metrics over time*. Kibana is for *text log search and analysis*. They are complementary.

| Tool       | What it stores      | Query style          | Best for                    |
|------------|---------------------|----------------------|-----------------------------|
| Prometheus | Numbers over time   | PromQL               | Latency, throughput, errors |
| Zipkin     | Request traces      | TraceId / service    | Where time is spent         |
| Kibana     | Log text + fields   | Full-text / KQL      | Debugging, log search       |

---

## How They Fit Together

```
Your Java App
  │
  ├── Micrometer ──────────→ Prometheus (scrapes /actuator/prometheus)
  │       └── Timers/Counters          └── Grafana (PromQL dashboards)
  │
  ├── MDC + Log4j2 ────────→ File / Loki / Logstash
  │       └── traceId in every line         └── Kibana (log search)
  │
  └── Micrometer Tracing ──→ Zipkin / Tempo
          └── Spans via OTLP           └── Trace waterfall UI
```
