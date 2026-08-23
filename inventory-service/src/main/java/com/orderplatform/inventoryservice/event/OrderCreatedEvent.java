package com.orderplatform.inventoryservice.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String customerId,
        List<OrderItemPayload> items,
        BigDecimal totalAmount,
        Instant createdAt
) {
    public record OrderItemPayload(String productId, int quantity, BigDecimal unitPrice) {
    }
}