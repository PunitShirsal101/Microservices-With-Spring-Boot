package com.enterprise.ecommerce.order.dto;

import com.enterprise.ecommerce.order.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating order status
 */
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String notes;

    // Constructors
    public UpdateOrderStatusRequest() {}

    public UpdateOrderStatusRequest(OrderStatus status) {
        this.status = status;
    }

    public UpdateOrderStatusRequest(OrderStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "UpdateOrderStatusRequest{" +
                "status=" + status +
                ", notes='" + notes + '\'' +
                '}';
    }
}