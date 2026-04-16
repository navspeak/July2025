# Kafka Consumer Poll, Position vs Commit, and Retry

## 1. Mental Model

```
position  = next offset to read (IN MEMORY)
commit    = last processed offset (IN KAFKA)
```

---

## 2. Where things live

```
           CONSUMER (APP MEMORY)          KAFKA BROKER
        ---------------------------     ------------------------
        position = 101                 committed = 99
        (next to read)                 (last processed)
```

---

## 3. Normal Flow

```
Initial:
position = 100
committed = 99

poll()
  -> returns record 100
  -> position becomes 101

process success

commit(100)

Final:
position = 101
committed = 100
```

---

## 4. Failure Case (NO retry yet)

```
Initial:
position = 100
committed = 99

poll()
  -> returns record 100
  -> position becomes 101

process -> EXCEPTION
NO commit

State:
position = 101   (in memory)
committed = 99   (in Kafka)

Next poll():
  -> returns 101 ❌
```

👉 Offset 100 is NOT retried automatically

---

## 5. Retry using seek()

```
poll()
  -> 100

process -> EXCEPTION

consumer.seek(partition, 100)

Next poll():
  -> 100 again ✅
```

---

## 6. Retry via restart

```
poll()
  -> 100

process -> EXCEPTION
NO commit

consumer crashes / rebalance

New consumer starts:
  -> reads committed offset = 99
  -> starts from 100

poll()
  -> 100 again ✅
```

---

## 7. Full Flow Diagram

```
                +----------------------+
                |   Kafka Broker       |
                |----------------------|
                | committed offset=99  |
                +----------+-----------+
                           |
                           |
                           v
        +--------------------------------------+
        |      Kafka Consumer (Spring)         |
        |--------------------------------------|
        | position = 100                       |
        |                                      |
        | poll() -> record 100                 |
        | position -> 101                      |
        |                                      |
        | process(record 100)                  |
        |     -> exception                     |
        |                                      |
        | NO commit                            |
        |                                      |
        | seek(100) (if retry enabled)         |
        |                                      |
        +--------------------------------------+
```

---

## 8. Sample Plain Kafka Consumer Code

```java
KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(List.of("orders"));

while (true) {
    ConsumerRecords<String, String> records =
            consumer.poll(Duration.ofMillis(100));

    for (ConsumerRecord<String, String> record : records) {
        try {
            System.out.println("Processing offset: " + record.offset());

            // simulate failure
            if (record.value().contains("fail")) {
                throw new RuntimeException("processing failed");
            }

            // success → commit
            consumer.commitSync();

        } catch (Exception e) {
            System.out.println("Error at offset: " + record.offset());

            // retry by seeking back
            consumer.seek(
                new TopicPartition(record.topic(), record.partition()),
                record.offset()
            );
        }
    }
}
```

---

## 9. Spring Kafka Equivalent (Conceptual)

```java
@KafkaListener(topics = "orders")
public void listen(String msg) {
    if (msg.contains("fail")) {
        throw new RuntimeException("fail");
    }
}
```

Spring internally does:

```
poll -> call listener
exception -> error handler
-> seek(offset)
-> retry
```

---

## 10. Key Takeaways

```
poll()   -> moves position forward
commit() -> makes progress durable
seek()   -> enables retry
restart  -> replays from committed offset
```

---

## 11. One-line Summary

```
Kafka does NOT retry automatically.
Retry = application responsibility using seek or restart.
```
