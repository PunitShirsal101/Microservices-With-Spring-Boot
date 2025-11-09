package com.enterprise.ecommerce.common.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer service for publishing events
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Send message to Kafka topic
     * @param topic the topic name
     * @param key the message key
     * @param message the message payload
     */
    @SuppressWarnings({"null", "nullness"})
    public void sendMessage(String topic, String key, Object message) {
        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, message);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Message sent successfully to topic: {}, partition: {}, offset: {}",
                            topic, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to send message to topic: {}", topic, ex);
                }
            });
        } catch (Exception e) {
            log.error("Error sending message to topic: {}", topic, e);
        }
    }

    /**
     * Send message to Kafka topic without key
     * @param topic the topic name
     * @param message the message payload
     */
    public void sendMessage(String topic, Object message) {
        sendMessage(topic, null, message);
    }
}