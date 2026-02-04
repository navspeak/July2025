# Resilience4j + CompletableFuture fan-out (Pricing + Inventory + DB) — End-to-end Sample

This is a **single, coherent example** showing how to:
- Call **Pricing** and **Inventory** (HTTP) in parallel using `CompletableFuture`
- Call the **DB** (blocking JDBC) safely
- Apply **Resilience4j** via **property-driven instances** using **programmatic decorators** (no AOP proxy gotchas)
- Use **timeouts + bulkheads + circuit breakers + optional retries**
- **Degrade smartly** (fallbacks) and **avoid cascades**

> Assumptions: Spring Boot (MVC or WebFlux), Java 17+, WebClient, JDBC/JPA (blocking).

---

## 1) Dependencies (Maven)

```xml
<dependencies>
  <!-- WebClient -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>

  <!-- JDBC (blocking) -->
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>

  <!-- Resilience4j Spring Boot (registries auto-configured from properties) -->
  <dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
  </dependency>
</dependencies>
```

---

## 2) application.yml (property-based policies)

### Resilience4j policies (3 instances: `pricing`, `inventory`, `db`)

```yaml
resilience4j:
  circuitbreaker:
    instances:
      pricing:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 5
      inventory:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 5
      db:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 20
        minimumNumberOfCalls: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 10s
        permittedNumberOfCallsInHalfOpenState: 5

  timelimiter:
    instances:
      pricing:
        timeoutDuration: 200ms
      inventory:
        timeoutDuration: 200ms
      db:
        timeoutDuration: 250ms

  bulkhead:
    instances:
      pricing:
        maxConcurrentCalls: 50
        maxWaitDuration: 0 #fail immediately  throw BulkheadFullExceptio
      inventory:
        maxConcurrentCalls: 30
        maxWaitDuration: 0

  # For blocking calls (JDBC), prefer a ThreadPoolBulkhead
  thread-pool-bulkhead:
    instances:
      db:
        coreThreadPoolSize: 20
        maxThreadPoolSize: 20
        queueCapacity: 50

  retry:
    instances:
      pricing:
        maxAttempts: 2
        waitDuration: 50ms
      inventory:
        maxAttempts: 2
        waitDuration: 50ms
```

### Hikari pool (DB concurrency cap)

```yaml
spring:
  datasource:
    hikari:
      maximumPoolSize: 20
      connectionTimeout: 2000
```

> **Tip:** align DB `thread-pool-bulkhead.db.maxThreadPoolSize` with `hikari.maximumPoolSize` (or slightly lower) so you don’t create more DB work than connections.

---

## 3) DTOs (simple)

```java
public record OrderRequest(String orderId, String productId, int qty) {}
public record OrderResponse(String orderId, String status, String priceSource, String invSource) {}
public record Price(String productId, double value, boolean stale) {}
public record Inventory(String productId, int available, boolean unknown) {}
public record OrderRow(String orderId, String status) {}
```

---

## 4) Clients + Repository

### PricingClient (HTTP → CompletableFuture)

```java
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Component
public class PricingClient {
  private final WebClient webClient;

  public PricingClient(WebClient.Builder builder) {
    this.webClient = builder.baseUrl("http://pricing-svc").build();
  }

  public CompletableFuture<Price> getPrice(String productId) {
    return webClient.get()
        .uri("/pricing/{id}", productId)
        .retrieve()
        .bodyToMono(Price.class)
        .toFuture();
  }
}
```

### InventoryClient (HTTP → CompletableFuture)

```java
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CompletableFuture;

@Component
public class InventoryClient {
  private final WebClient webClient;

  public InventoryClient(WebClient.Builder builder) {
    this.webClient = builder.baseUrl("http://inventory-svc").build();
  }

  public CompletableFuture<Inventory> getInventory(String productId) {
    return webClient.get()
        .uri("/inventory/{id}", productId)
        .retrieve()
        .bodyToMono(Inventory.class)
        .toFuture();
  }
}
```

### OrderRepository (blocking JDBC)

```java
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderRepository {
  private final JdbcTemplate jdbc;

  public OrderRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public OrderRow findById(String orderId) {
    return jdbc.queryForObject(
        "select order_id, status from orders where order_id=?",
        (rs, rowNum) -> new OrderRow(rs.getString("order_id"), rs.getString("status")),
        orderId
    );
  }
}
```

---

## 5) The Service: one method doing Pricing + Inventory + DB with Resilience4j

This uses **property-driven registries** (`CircuitBreakerRegistry`, `BulkheadRegistry`, etc.) that Spring Boot auto-configures from `application.yml`.

### The key idea
- Wrap each downstream call with **Decorators**
- Apply **Bulkhead + TimeLimiter + CircuitBreaker (+ Retry)**
- Add **fallback** with `handle()`
- Compose using `thenCombine`
- **Block only once** at the controller boundary (or return the `CompletableFuture`)

```java
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.bulkhead.ThreadPoolBulkhead;
import io.github.resilience4j.bulkhead.ThreadPoolBulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
import java.util.function.Supplier;

@Service
public class OrderEnrichmentService {

  private final PricingClient pricingClient;
  private final InventoryClient inventoryClient;
  private final OrderRepository orderRepository;

  private final CircuitBreakerRegistry cbRegistry;
  private final BulkheadRegistry bulkheadRegistry;
  private final ThreadPoolBulkheadRegistry tpBulkheadRegistry;
  private final TimeLimiterRegistry timeLimiterRegistry;
  private final RetryRegistry retryRegistry;

  // Scheduler needed by TimeLimiter for async
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  public OrderEnrichmentService(
      PricingClient pricingClient,
      InventoryClient inventoryClient,
      OrderRepository orderRepository,
      CircuitBreakerRegistry cbRegistry,
      BulkheadRegistry bulkheadRegistry,
      ThreadPoolBulkheadRegistry tpBulkheadRegistry,
      TimeLimiterRegistry timeLimiterRegistry,
      RetryRegistry retryRegistry
  ) {
    this.pricingClient = pricingClient;
    this.inventoryClient = inventoryClient;
    this.orderRepository = orderRepository;
    this.cbRegistry = cbRegistry;
    this.bulkheadRegistry = bulkheadRegistry;
    this.tpBulkheadRegistry = tpBulkheadRegistry;
    this.timeLimiterRegistry = timeLimiterRegistry;
    this.retryRegistry = retryRegistry;
  }

  public CompletableFuture<OrderResponse> enrich(OrderRequest req) {

    // ---- Pricing policy (props-driven instance name: "pricing") ----
    CircuitBreaker pricingCb = cbRegistry.circuitBreaker("pricing");
    Bulkhead pricingBh = bulkheadRegistry.bulkhead("pricing");
    TimeLimiter pricingTl = timeLimiterRegistry.timeLimiter("pricing");
    Retry pricingRetry = retryRegistry.retry("pricing");

    Supplier<CompletionStage<Price>> pricingSupplier = () -> pricingClient.getPrice(req.productId());
    Supplier<CompletionStage<Price>> decoratedPricing =
        Decorators.ofSupplier(pricingSupplier)
            .withBulkhead(pricingBh)
            .withTimeLimiter(pricingTl, scheduler)
            .withCircuitBreaker(pricingCb)
            .withRetry(pricingRetry)     // keep small & safe
            .decorate();

    CompletableFuture<Price> priceFuture =
        decoratedPricing.get().toCompletableFuture()
            .handle((val, ex) -> ex != null
                ? new Price(req.productId(), 0.0, true)   // fallback: cached/stale marker (example)
                : val);

    // ---- Inventory policy (props-driven instance name: "inventory") ----
    CircuitBreaker invCb = cbRegistry.circuitBreaker("inventory");
    Bulkhead invBh = bulkheadRegistry.bulkhead("inventory");
    TimeLimiter invTl = timeLimiterRegistry.timeLimiter("inventory");
    Retry invRetry = retryRegistry.retry("inventory");

    Supplier<CompletionStage<Inventory>> invSupplier = () -> inventoryClient.getInventory(req.productId());
    Supplier<CompletionStage<Inventory>> decoratedInv =
        Decorators.ofSupplier(invSupplier)
            .withBulkhead(invBh)
            .withTimeLimiter(invTl, scheduler)
            .withCircuitBreaker(invCb)
            .withRetry(invRetry)
            .decorate();

    CompletableFuture<Inventory> invFuture =
        decoratedInv.get().toCompletableFuture()
            .handle((val, ex) -> ex != null
                ? new Inventory(req.productId(), 0, true) // fallback: unknown inventory
                : val);

    // ---- DB policy (blocking JDBC) - use ThreadPoolBulkhead ----
    CircuitBreaker dbCb = cbRegistry.circuitBreaker("db");
    ThreadPoolBulkhead dbTpb = tpBulkheadRegistry.threadPoolBulkhead("db");
    TimeLimiter dbTl = timeLimiterRegistry.timeLimiter("db");

    Supplier<CompletionStage<OrderRow>> dbSupplier =
        () -> CompletableFuture.supplyAsync(
            () -> orderRepository.findById(req.orderId()),
            dbTpb.getExecutorService()  // isolated, bounded pool from properties
        );

    Supplier<CompletionStage<OrderRow>> decoratedDb =
        Decorators.ofSupplier(dbSupplier)
            .withTimeLimiter(dbTl, scheduler)
            .withCircuitBreaker(dbCb)
            .decorate();

    CompletableFuture<OrderRow> dbFuture =
        decoratedDb.get().toCompletableFuture()
            .handle((val, ex) -> ex != null
                ? new OrderRow(req.orderId(), "UNKNOWN")  // fallback row
                : val);

    // ---- Compose (no blocking until the end) ----
    return priceFuture
        .thenCombine(invFuture, (price, inv) -> new Object[]{price, inv})
        .thenCombine(dbFuture, (pi, row) -> {
          Price price = (Price) pi[0];
          Inventory inv = (Inventory) pi[1];

          String priceSource = price.stale() ? "FALLBACK" : "LIVE";
          String invSource = inv.unknown() ? "FALLBACK" : "LIVE";

          return new OrderResponse(row.orderId(), row.status(), priceSource, invSource);
        });
  }
}
```

---

## 6) Controller (MVC) — block once at boundary

```java
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
  private final OrderEnrichmentService svc;

  public OrderController(OrderEnrichmentService svc) {
    this.svc = svc;
  }

  @PostMapping("/enrich")
  public OrderResponse enrich(@RequestBody OrderRequest req) {
    return svc.enrich(req).join(); // single join point
  }
}
```

> If you are using WebFlux controller, prefer returning `Mono<OrderResponse>` via:  
> `return Mono.fromFuture(svc.enrich(req));`

---

## 7) Why this avoids cascades (quick reasoning)

- **Timeouts**: `TimeLimiter` bounds wait time for each dependency  
- **Bulkheads**: Pricing/Inventory concurrency is capped (semaphore bulkhead)  
- **DB isolation**: DB uses **ThreadPoolBulkhead** with bounded queue; aligned with Hikari pool  
- **Circuit Breakers**: stop hammering a failing dependency  
- **Retries**: kept small; only safe if the operation is idempotent  
- **Degradation**: each future converts failures into safe fallbacks via `handle()`  
- **Composition first**: `thenCombine` composes futures; only one `join()` at the boundary  

---

## 8) Production signals to monitor

- Hikari: active / idle / pending threads / acquire time  
- Thread pools: active threads, queue depth, rejection count  
- Resilience4j: CB state, failure rate, not-permitted calls, retries, timeouts  
- Per-dependency latency p95/p99  

---

## 9) Notes / gotchas

- Don’t do JDBC or `.block()` on reactive event-loop threads  
- Keep retry small; never retry overload signals  
- Align DB concurrency limits (ThreadPoolBulkhead + Hikari maxPoolSize)  
- If using annotations, remember AOP proxy rules (public method, no self-invocation)  
