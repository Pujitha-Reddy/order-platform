package com.orderplatform.paymentservice.service;

import com.orderplatform.fraudcheckservice.grpc.FraudCheckRequest;
import com.orderplatform.fraudcheckservice.grpc.FraudCheckResponse;
import com.orderplatform.fraudcheckservice.grpc.FraudCheckServiceGrpc;
import com.orderplatform.paymentservice.event.InventoryReservedEvent;
import com.orderplatform.paymentservice.event.PaymentCompletedEvent;
import com.orderplatform.paymentservice.event.PaymentFailedEvent;
import com.orderplatform.paymentservice.model.Payment;
import com.orderplatform.paymentservice.model.PaymentStatus;
import com.orderplatform.paymentservice.repository.PaymentRepository;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String PAYMENT_TOPIC = "payment.events";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FraudCheckServiceGrpc.FraudCheckServiceBlockingStub fraudCheckStub;

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {

        if (paymentRepository.existsByOrderId(event.orderId())) {
            log.warn("Order {} already has a payment record — skipping duplicate delivery", event.orderId());
            return;
        }

        FraudCheckResponse fraudResponse;
        try {
            FraudCheckRequest fraudRequest = FraudCheckRequest.newBuilder()
                    .setOrderId(event.orderId().toString())
                    .setCustomerId(event.customerId())
                    .setAmount(event.totalAmount().doubleValue())
                    .build();

            log.info("Calling fraud-check-service for order {}", event.orderId());
            fraudResponse = fraudCheckStub.checkFraud(fraudRequest);

        } catch (StatusRuntimeException e) {
            log.error("gRPC call to fraud-check-service failed for order {}: {}", event.orderId(), e.getStatus());
            recordAndPublishFailure(event, "Fraud check service unavailable: " + e.getStatus().getCode());
            return;
        }

        boolean approved = fraudResponse.getApproved();

        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .amount(event.totalAmount())
                .fraudRiskScore(fraudResponse.getRiskScore())
                .status(approved ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .failureReason(approved ? null : fraudResponse.getReason())
                .build();

        paymentRepository.save(payment);

        if (approved) {
            kafkaTemplate.send(PAYMENT_TOPIC, event.orderId().toString(),
                    new PaymentCompletedEvent(event.orderId(), event.customerId(), fraudResponse.getRiskScore()));
            log.info("Payment completed for order {} (riskScore={})", event.orderId(), fraudResponse.getRiskScore());
        } else {
            kafkaTemplate.send(PAYMENT_TOPIC, event.orderId().toString(),
                    new PaymentFailedEvent(event.orderId(), event.customerId(), fraudResponse.getReason()));
            log.warn("Payment failed for order {}: {}", event.orderId(), fraudResponse.getReason());
        }
    }

    private void recordAndPublishFailure(InventoryReservedEvent event, String reason) {
        Payment payment = Payment.builder()
                .orderId(event.orderId())
                .customerId(event.customerId())
                .amount(event.totalAmount())
                .status(PaymentStatus.FAILED)
                .failureReason(reason)
                .build();
        paymentRepository.save(payment);

        kafkaTemplate.send(PAYMENT_TOPIC, event.orderId().toString(),
                new PaymentFailedEvent(event.orderId(), event.customerId(), reason));
    }
}