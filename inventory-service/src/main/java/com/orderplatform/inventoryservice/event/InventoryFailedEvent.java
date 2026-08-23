package com.orderplatform.inventoryservice.event;

import java.util.UUID;

public record InventoryFailedEvent(
        UUID orderId,
        String reason
) {
}