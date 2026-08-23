package com.orderplatform.orderservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderplatform.orderservice.model.OrderStatus;
import com.orderplatform.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaOutcomeConsumer {

    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    @KafkaListener(topics = {"inventory.events", "payment.events"}, groupId = "order-service-group")
    public void consume(String rawJson,
                         org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            UUID orderId = UUID.fromString(node.get("orderId").asText());

            switch (record.topic()) {
                case "inventory.events" -> {
                    boolean failed = node.has("reason") && !node.has("riskScore");
                    if (failed) {
                        log.info("Order {} cancelled — inventory unavailable", orderId);
                        orderService.updateStatus(orderId, OrderStatus.CANCELLED);
                    }
                    // Reservation success doesn't finalize the order yet — payment still pending.
                }
                case "payment.events" -> {
                    boolean completed = node.has("riskScore");
                    OrderStatus finalStatus = completed ? OrderStatus.COMPLETED : OrderStatus.CANCELLED;
                    log.info("Order {} reached final status {}", orderId, finalStatus);
                    orderService.updateStatus(orderId, finalStatus);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process saga outcome event: {}", e.getMessage(), e);
        }
    }
}