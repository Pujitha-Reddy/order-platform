package com.orderplatform.inventoryservice.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InventoryReservedEvent(
        UUID orderId,
        String customerId,
        List<ReservedItem> items,
        BigDecimal totalAmount
) {
    public record ReservedItem(String productId, int quantity) {
    }
}