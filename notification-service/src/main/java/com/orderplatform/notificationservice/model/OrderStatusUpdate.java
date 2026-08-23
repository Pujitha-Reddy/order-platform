package com.orderplatform.notificationservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderStatusUpdate(
        String orderId,
        String customerId,
        String productId,
        BigDecimal amount,
        String status,
        String detail,
        Instant updatedAt
) {
    public static OrderStatusUpdate initial(String orderId, String customerId, String productId, BigDecimal amount) {
        return new OrderStatusUpdate(orderId, customerId, productId, amount, "CREATED", null, Instant.now());
    }

    public OrderStatusUpdate withStatus(String newStatus, String detail) {
        return new OrderStatusUpdate(orderId, customerId, productId, amount, newStatus, detail, Instant.now());
    }
}