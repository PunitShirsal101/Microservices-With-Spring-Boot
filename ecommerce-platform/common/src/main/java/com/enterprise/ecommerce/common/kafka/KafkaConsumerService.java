package com.enterprise.ecommerce.common.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

/**
 * Base Kafka consumer service
 */
@Service
@Slf4j
public class KafkaConsumerService {

    /**
     * Generic Kafka listener for handling messages
     * Services can extend this or create their own listeners
     */
    @KafkaListener(topics = "#{'${spring.kafka.consumer.topics:}'.split(',')}",
                   groupId = "#{${spring.kafka.consumer.group-id}}",
                   containerFactory = "kafkaListenerContainerFactory")
    public void listen(@org.springframework.lang.NonNull ConsumerRecord<String, Object> consumerRecord, @org.springframework.lang.NonNull Acknowledgment acknowledgment) {
        try {
            log.info("Received message: key={}, value={}, topic={}, partition={}, offset={}",
                    consumerRecord.key(), consumerRecord.value(), consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());

            // Process the message here or delegate to specific handlers
            processMessage(consumerRecord);

            // Manually acknowledge the message
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Error processing message: {}", consumerRecord.value(), e);
            // In a real application, you might want to send to a dead letter topic
        }
    }

    /**
     * Process the incoming message
     * Override this method in subclasses for specific message handling
     */
    protected void processMessage(@org.springframework.lang.NonNull ConsumerRecord<String, Object> consumerRecord) {
        // Default implementation - log the message
        log.debug("Processing message: {}", consumerRecord.value());
    }
}