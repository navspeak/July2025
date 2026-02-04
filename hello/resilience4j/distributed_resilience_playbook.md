# Handling Failures in Distributed Systems — A Practical Playbook

> **Goal**: Move from *per-service resilience* (Resilience4j) to *system-wide resilience*.

This document is written for **Spring Boot / microservice architects** and is suitable for **production design reviews and interviews**.

---

## 1. First Principle: Bound Time Everywhere

If time is unbounded, failure spreads.

Apply timeouts at **every hop**:

- HTTP client (connect + response)
- Server request handling
- Database calls
- Message consumption / polling

**Rule of thumb**:
> Each downstream timeout must be **smaller** than the caller’s overall SLA budget.

Example:
- API SLA: 800 ms
- Service A → B timeout: 300 ms
- Service B → DB timeout: 150 ms

---

## 2. Circuit Breakers + Bulkheads (Per Dependency)

### Circuit Breaker
Stops repeated calls to a failing or slow dependency.

Use **one circuit breaker per downstream dependency**, not per service:
- Order → Pricing
- Order → Inventory
- Order → Payment

### Bulkhead
Prevents one dependency from consuming all threads or connections.

Apply:
- Separate thread pools per downstream
- Separate connection pools

**Outcome**: Failure is isolated instead of cascading.

---

## 3. Retries — Carefully and Selectively

Retries can **amplify outages** if misused.

### Safe to Retry
- Network errors
- 502 / 503 / 504
- Idempotent operations (GET, PUT)

### Unsafe to Retry
- Non-idempotent POST without idempotency key
- 4xx client errors (except controlled cases)

### Mandatory Rules
- Max attempts: 2–3
- Exponential backoff
- **Jitter** (to avoid retry storms)

---

## 4. Idempotency & Deduplication (Distributed Reality)

In distributed systems:
- Clients retry
- Messages duplicate
- Timeouts lie

### Command APIs (REST)
- Require **Idempotency-Key**
- Persist request key → outcome
- Enforce uniqueness at DB level

### Event Consumers
- Deduplicate by message ID
- Design handlers to be idempotent

> Exactly-once is rare. Aim for **at-least-once + idempotent handling**.

---

## 5. Graceful Degradation (Business-Aware Resilience)

Failing fast is not enough — **degrade smartly**.

Examples:
- Pricing down → serve cached price + warning banner
- Inventory down → accept order as *pending*
- Recommendation service down → hide widget, not page

This requires collaboration with **product & UX**.

---

## 6. Load Shedding & Backpressure

When overloaded:

- Return **429** early
- Limit fan-out per request
- Prioritize critical traffic
- Queue non-critical work asynchronously

Protect the core business path at all costs.

---

## 7. Data Consistency Strategy

Strong consistency everywhere is impossible.

Choose deliberately:

- **Saga / orchestration** for multi-step workflows
- **Outbox pattern** for reliable event publishing
- **Compensating actions** instead of rollbacks
- Eventual consistency with clear states

---

## 8. Observability = Resilience

You cannot manage failures you cannot see.

Minimum requirements:
- Trace ID propagated across services
- Per-dependency metrics (latency, error rate, CB state)
- Logs with correlation IDs
- SLOs + burn-rate alerts

Observability is not optional — it’s part of correctness.

---

## 9. Infrastructure-Level Resilience

Service-level controls are insufficient if shared infrastructure fails.

Plan for:
- Regional isolation
- Multi-AZ or multi-region deployments
- Dependency failover (DB replicas, alternate providers)
- Feature flags for instant degradation

---

## 10. Mental Model (Interview Gold)

> **Bound time → Isolate resources → Avoid amplification → Tolerate duplicates → Degrade gracefully → Observe and adapt**

---

## 11. How Resilience4j Fits In

Resilience4j handles **local, per-dependency resilience**:
- Circuit breakers
- Retries
- Bulkheads
- Time limiters

It is necessary — but **not sufficient** — for distributed systems.

---

## 12. Common Failure Modes This Prevents

- Thread pool exhaustion
- Retry storms
- Cascading timeouts
- Partial outages taking down full system
- Duplicate writes / double payments

---

## 13. Summary

Resilience in distributed systems is **layered**:

1. Timeouts
2. Isolation
3. Controlled retries
4. Idempotency
5. Degradation
6. Backpressure
7. Observability
8. Infrastructure strategy

No single library solves this — but together, these patterns do.

---

*This document is intentionally technology-agnostic but maps cleanly to Spring Boot, Resilience4j, Kafka, and cloud-native platforms.*