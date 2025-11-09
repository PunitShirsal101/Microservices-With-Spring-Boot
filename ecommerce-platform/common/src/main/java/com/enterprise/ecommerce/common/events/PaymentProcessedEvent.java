package com.enterprise.ecommerce.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event fired when a payment is processed
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent {
    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime processedAt;

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING, CANCELLED
    }
}