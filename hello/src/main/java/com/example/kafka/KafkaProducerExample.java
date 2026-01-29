package com.example.kafka;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class KafkaProducerExample {

    public static void main(String[] args) throws Exception {
        String topicName = "async-test-topic";

        System.out.println("Executing asynchronous Kafka producer");

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "1");
        /*
        When set to 0, the producer does not wait for any acknowledgment from the broker and assumes the message has been sent successfully.
        Setting it to 1 requires the producer to receive an acknowledgment from the leader broker.
        Setting it to all requires acknowledgment from all in-sync replicas. => Highest durability
        Read broker/topic config:    // min.insync.replicas =>
         */
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        // fot ENABLE_IDEMPOTENCE_CONFIG true, kafka internally sets
        // ack = all, retries = int_max, max.in.flight.requests.per.connection <=5 (kafka tracts a small sliding window of sequence numbers)
        //
        Producer<String, String> producer = new KafkaProducer<>(properties);

        CountDownLatch counter = new CountDownLatch(5);

        for (int i = 0; i < 5; i++) {

            String key = "async-key-" + i;
            String value = "async-value-" + i;

            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);

            producer.send(record, new Callback() {

                @Override
                public void onCompletion(RecordMetadata metadata, Exception e) {
                    if (e != null) {
                        System.out.println("Error sending data " + key + e.getMessage());
                    } else {
                        System.out.println("Produced data: " + key + "=" + value);
                    }
                    counter.countDown();
                }

            });

            Thread.sleep(1000);
        }

        counter.await();

        producer.flush();
        producer.close();

        System.out.println("End of program");
    }
}