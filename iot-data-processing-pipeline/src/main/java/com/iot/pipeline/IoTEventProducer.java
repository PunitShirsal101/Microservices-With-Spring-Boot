package com.iot.pipeline;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

public class IoTEventProducer {
    private final KafkaProducer<String, String> producer;

    public IoTEventProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", Config.getKafkaBootstrapServers());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = new KafkaProducer<>(props);
    }

    public void sendEvent(String topic, String key, String event) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, event);
        producer.send(record);
        producer.flush();
        System.out.println("Sent event: " + event);
    }

    public void close() {
        producer.close();
    }
}
