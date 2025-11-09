package com.iot.pipeline;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class IoTEventConsumer {
    private final KafkaConsumer<String, String> consumer;
    private final ElasticsearchClient esClient;

    public IoTEventConsumer(ElasticsearchClient esClient) {
        Properties props = new Properties();
        props.put("bootstrap.servers", Config.getKafkaBootstrapServers());
        // use a unique group id per run so we read from the beginning
        props.put("group.id", "iot-consumer-" + System.currentTimeMillis());
        props.put("auto.offset.reset", "earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        this.consumer = new KafkaConsumer<>(props);
        this.esClient = esClient;
    }

    public void consumeAndIndex(String topic) throws IOException {
        createIndexIfNotExists();
        consumer.subscribe(Collections.singletonList(topic));
        // trigger partition assignment
        consumer.poll(Duration.ofMillis(100));
        // seek to beginning of assigned partitions to read all messages
        Set<TopicPartition> partitions = consumer.assignment();
        if (!partitions.isEmpty()) {
            consumer.seekToBeginning(partitions);
        }

        for (int i = 0; i < 20; i++) {  // increase polls slightly to allow consumer to read
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(200));
            for (var record : records) {
                esClient.index(idx -> idx.index(Config.getElasticsearchIndex()).withJson(new StringReader(record.value())).refresh(Refresh.True));
                System.out.println("Consumed and indexed: " + record.value());
            }
        }
    }

    private void createIndexIfNotExists() throws IOException {
        String indexName = Config.getElasticsearchIndex();
        boolean exists = esClient.indices().exists(e -> e.index(indexName)).value();
        if (!exists) {
            esClient.indices().create(c -> c.index(indexName));
            System.out.println("Created index: " + indexName);
        }
    }

    public void close() throws IOException {
        consumer.close();
        // esClient is closed externally
    }
}
