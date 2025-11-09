package com.enterprise.ecommerce.order.dto;

import com.enterprise.ecommerce.order.entity.ShippingAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for creating a new order
 */
public class CreateOrderRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddress shippingAddress;

    @PositiveOrZero(message = "Discount amount must be positive or zero")
    private BigDecimal discountAmount;

    private String notes;

    // Constructors
    public CreateOrderRequest() {}

    public CreateOrderRequest(Long userId, List<OrderItemRequest> items, ShippingAddress shippingAddress) {
        this.userId = userId;
        this.items = items;
        this.shippingAddress = shippingAddress;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "userId=" + userId +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                ", shippingAddress=" + shippingAddress +
                '}';
    }
}