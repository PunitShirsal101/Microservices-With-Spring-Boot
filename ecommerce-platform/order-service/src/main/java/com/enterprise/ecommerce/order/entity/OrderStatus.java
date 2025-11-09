package com.enterprise.ecommerce.order.entity;

/**
 * Enumeration for order status
 */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}