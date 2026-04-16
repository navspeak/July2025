# What @RetryableTopic does

It automatically creates:
```
orders (main topic)
orders-retry-0
orders-retry-1
orders-retry-2
orders-dlt
```
and wires consumers for all of them.
```java
@RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 2000)
)
@KafkaListener(topics = "orders")
public void listen(String msg) {
    if (msg.contains("fail")) {
        throw new RuntimeException("fail");
    }
    System.out.println("processed: " + msg);
}
```
# Full flow (ASCII)
## STEP 1: Normal consumption
```
Client Producer
|
v
Kafka: orders topic
|
v
Consumer (your @KafkaListener)
|
v
process(msg)
|
v
❌ exception thrown
```
- Instead of retrying in-memory… Spring does this:
## STEP 2: publish to retry topic
```
orders  --fail-->  orders-retry-0
```
## STEP 3: delayed retry
```
orders-retry-0  (after delay)
|
v
listener consumes again
|
v
process(msg)
|
v
❌ fails again
```
## STEP 4: next retry topic
```
orders-retry-0 --fail--> orders-retry-1
```
### STEP 5: final retry
```
orders-retry-1 --fail--> orders-dlt
```
# Visual pipeline
```
   orders
   |
   v
   [Consumer]
   |
   | fail
   v
   orders-retry-0 (2s delay)
   |
   | fail
   v
   orders-retry-1 (2s delay)
   |
   | fail
   v
   orders-dlt (dead letter)
```
# What actually happens internally
- When exception happen. Spring does:
1. catch exception
2. produce same message to retry topic
3. commit original offset

👉 This is critical:

>> Offset IS committed → message will NOT be re-polled from original topic

6. Why this is powerful
   Non-blocking
   Thread is free immediately

Instead of:

sleep + retry + block partition ❌
Durable retries

Retry message is:

stored in Kafka

So:

survives crash
survives restart
survives rebalance
Scalable

Each retry topic:

can have its own consumer group
can scale independently
7. Wire-level view
   Consumer receives message from:

orders topic

On failure:
-> produces to orders-retry-0

Later:
Consumer polls:

orders-retry-0

On failure:
-> produces to orders-retry-1

So retries are just new Kafka messages.

8. Compare with in-memory retry
   Feature	In-memory retry	@RetryableTopic
   Where retry happens	inside container	Kafka topics
   Blocking	yes	no
   survives crash	no	yes
   complexity	low	medium
   scalability	limited	high
9. Headers (important)

Spring adds retry metadata:

RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS

You can read it:

@KafkaListener(topics = "orders")
public void listen(
String msg,
@Header(name = RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS, required = false)
Integer attempt
) {
System.out.println("attempt = " + attempt);
}
10. DLT (Dead Letter Topic)

Final failure goes to:

orders-dlt

You can handle it:

@DltHandler
public void dlt(String msg) {
System.out.println("DLT message: " + msg);
}
11. Important nuance

This is NOT Kafka’s built-in retry.

Kafka itself has no retry concept.

This is:

Spring Kafka implementing retry USING Kafka topics
12. One-line mental model
    Retry = "republish message to another topic with delay"
13. Interview-ready answer

@RetryableTopic implements non-blocking retries by publishing failed messages to retry topics with delays. 
Each retry is a new Kafka message, and offsets from the original topic are committed, making retries durable and scalable.