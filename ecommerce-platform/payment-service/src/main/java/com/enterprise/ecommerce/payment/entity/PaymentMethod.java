package com.enterprise.ecommerce.payment.entity;

/**
 * Enum for payment methods
 */
public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    PAYPAL("PayPal"),
    STRIPE("Stripe"),
    BANK_TRANSFER("Bank Transfer"),
    APPLE_PAY("Apple Pay"),
    GOOGLE_PAY("Google Pay"),
    CRYPTOCURRENCY("Cryptocurrency"),
    CASH_ON_DELIVERY("Cash on Delivery");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}