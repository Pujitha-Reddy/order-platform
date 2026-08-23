package com.orderplatform.orderservice.dto;

import com.orderplatform.orderservice.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        List<OrderItemResponse> items,
        BigDecimal totalAmount,
        String status,
        Instant createdAt
) {
    public record OrderItemResponse(String productId, int quantity, BigDecimal unitPrice) {
    }

    public static OrderResponse from(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(i -> new OrderItemResponse(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                items,
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}