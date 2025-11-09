package com.enterprise.ecommerce.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Base event class for Kafka messaging
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseEvent {
    
    private String eventId;
    private String eventType;
    private String source;
    private String version;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> data;
    private Map<String, String> metadata;
    
    public static BaseEvent create(String eventType, String source, Map<String, Object> data) {
        return BaseEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .eventType(eventType)
                .source(source)
                .version("1.0")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
}