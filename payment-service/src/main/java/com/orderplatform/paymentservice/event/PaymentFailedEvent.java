package com.orderplatform.paymentservice.event;

import java.util.UUID;

public record PaymentFailedEvent(
        UUID orderId,
        String customerId,
        String reason
) {
}