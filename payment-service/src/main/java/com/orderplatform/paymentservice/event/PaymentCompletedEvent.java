package com.orderplatform.paymentservice.event;

import java.util.UUID;

public record PaymentCompletedEvent(
        UUID orderId,
        String customerId,
        double riskScore
) {
}