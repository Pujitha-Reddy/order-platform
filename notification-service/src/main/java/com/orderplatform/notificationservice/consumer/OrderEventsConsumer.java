package com.orderplatform.notificationservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderplatform.notificationservice.model.OrderStatusUpdate;
import com.orderplatform.notificationservice.service.OrderStatusCacheService;
import com.orderplatform.notificationservice.websocket.OrderStatusWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final ObjectMapper redisObjectMapper;
    private final OrderStatusCacheService cacheService;
    private final OrderStatusWebSocketHandler webSocketHandler;

    @KafkaListener(
            topics = {"order.events", "inventory.events", "payment.events"},
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String rawJson, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        try {
            JsonNode node = redisObjectMapper.readTree(rawJson);
            String topic = record.topic();

            OrderStatusUpdate update = switch (topic) {
                case "order.events" -> handleOrderCreated(node);
                case "inventory.events" -> handleInventoryEvent(node);
                case "payment.events" -> handlePaymentEvent(node);
                default -> null;
            };

            if (update != null) {
                cacheService.save(update);
                webSocketHandler.broadcast(update);
                log.info("Updated status for order {}: {}", update.orderId(), update.status());
            }

        } catch (Exception e) {
            log.error("Failed to process message from topic {}: {}", record.topic(), e.getMessage(), e);
        }
    }

    private OrderStatusUpdate handleOrderCreated(JsonNode node) {
        String orderId = node.get("orderId").asText();
        String customerId = node.get("customerId").asText();
        String productId = node.get("productId").asText();
        BigDecimal amount = new BigDecimal(node.get("totalAmount").asText());
        return OrderStatusUpdate.initial(orderId, customerId, productId, amount);
    }

    private OrderStatusUpdate handleInventoryEvent(JsonNode node) {
        String orderId = node.get("orderId").asText();
        var existing = cacheService.get(orderId);
        boolean reserved = node.has("quantity") && !node.has("reason");

        if (existing.isEmpty()) {
            log.warn("Received inventory event for unknown order {} — cache miss", orderId);
            return null;
        }

        return reserved
                ? existing.get().withStatus("INVENTORY_RESERVED", null)
                : existing.get().withStatus("INVENTORY_FAILED", node.path("reason").asText(null));
    }

    private OrderStatusUpdate handlePaymentEvent(JsonNode node) {
        String orderId = node.get("orderId").asText();
        var existing = cacheService.get(orderId);
        boolean completed = node.has("riskScore");

        if (existing.isEmpty()) {
            log.warn("Received payment event for unknown order {} — cache miss", orderId);
            return null;
        }

        return completed
                ? existing.get().withStatus("PAYMENT_COMPLETED", null)
                : existing.get().withStatus("PAYMENT_FAILED", node.path("reason").asText(null));
    }
}