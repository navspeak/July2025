package com.example.kafka;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;


//                +--------------------------------------+
//                |                KAFKA                 |
//                |                                      |
//                |   +-----------+     +-----------+    |
//                |   |  Topic 1  |     |  Topic 2  |    |
//                |   +-----------+     +-----------+    |
//                |                                      |
//                +--------------------------------------+
//                ^            ^            ^
//                |            |            |
//                | subscribe  |   poll     | commit offset
//                |            |            |
//                v            v            v
//                +--------------------------------------+
//                |              CONSUMERS               |
//                |        (Consumer Group / App)        |
//                +--------------------------------------+
//                            |
//                            | process
//                            v
//                +--------------------------------------+
//                |          EXTERNAL SERVICE            |
//                +--------------------------------------+


/* TERMINAL 1
Zookeeper: (zookeeper-server-start) This acts as the "manager" or "coordinator." It keeps track of the status of Kafka nodes and topics.
         # /app/confluent-7.9.1/bin/zookeeper-server-start /app/confluent-7.9.1/etc/kafka/zookeeper.properties >/dev/null&
Kafka Broker: (kafka-server-start) This is the actual engine that handles the storage and transmission of messages.
        #/app/confluent-7.9.1/bin/kafka-server-start /app/confluent-7.9.1/etc/kafka/server.properties >/dev/null&
Creating Topics: created two "folders" (topics) where messages can be sent:
    queue-test: Configured with 2 partitions.
    topic-test: Also configured with 2 partitions.
            # /app/confluent-7.9.1/bin/kafka-topics --bootstrap-server localhost:9092 --create --topic queue-test --partitions 2 --replication-factor 1
            Created topic queue-test.

            # /app/confluent-7.9.1/bin/kafka-topics --bootstrap-server localhost:9092 --create --topic topic-test --partitions 2 --replication-factor 1
            Created topic topic-test.

    # mkdir -p app/kafka-course/consumer-api/src/main/java/com/example
    # cp -r /usercode/KafkaConsumerExample.java /app/kafka-course/consumer-api/src/main/java/com/example/KafkaConsumerExample.java
    # mkdir -p com/example
    # cp -r usercode/KafkaConsumerExample.java com/example/
    # javac -cp ".:/opt/kafka/libs/*" com/example/KafkaConsumerExample.java
    # java -cp ".:/opt/kafka/libs/*" com.example.KafkaConsumerExample

*/
// TERMINAL 2


public class KafkaConsumerExample {
    public static void main(String[] args) {

        String defaultTopicName = "test-topic";
        String defaultGroupName = "test-group";

        String topicName = System.getenv("TOPIC_NAME") != null ? System.getenv("TOPIC_NAME") : defaultTopicName;
        String groupName = System.getenv("CONSUMER_GROUP_NAME") != null ? System.getenv("CONSUMER_GROUP_NAME") : defaultGroupName;

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        //props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
        // => applicable when ENABLE_AUTO_COMMIT_CONFIG = true| auto commits at 5 secs |
        // => enabling auto-commit can result in data loss if the consumer dies before the commit.
        // Additionally, if we set the auto.commit.interval.ms configuration to a very low value, the consumer may spend
        // too much time committing offsets and not enough time processing messages, leading to a decrease in overall throughput.

        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping Kafka Consumer");
            consumer.close();
        }));

        System.out.println("Started Kafka Consumer for Topic: "+ topicName + ", Consumer Group: " + groupName);


        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Got message from topic=%s, partition=%d, offset=%d, key=%s, value=%s\n",
                            record.topic(), record.partition(), record.offset(), record.key(), record.value());

                    consumer.commitAsync(new OffsetCommitCallback() {
                        @Override
                        public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
                            if (exception != null) {
                                System.out.println("Exception while committing offsets - "+ offsets);
                            } else {
                                for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : offsets.entrySet()) {

                                    System.out.printf("Commit details: topic=%s, partition=%d, offset=%d\n",
                                            entry.getKey().topic(), entry.getKey().partition(), entry.getValue().offset());

                                }
                            }
                        }
                    });
                }
            }
        } finally {
            consumer.close();
        }
    }

}