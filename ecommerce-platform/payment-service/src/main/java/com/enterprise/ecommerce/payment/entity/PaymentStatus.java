package com.enterprise.ecommerce.payment.entity;

/**
 * Enum for payment status
 */
public enum PaymentStatus {
    PENDING("Payment is pending"),
    PROCESSING("Payment is being processed"),
    COMPLETED("Payment completed successfully"),
    FAILED("Payment failed"),
    CANCELLED("Payment was cancelled"),
    REFUNDED("Payment was refunded"),
    PARTIALLY_REFUNDED("Payment was partially refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.name() + " (" + description + ")";
    }
}