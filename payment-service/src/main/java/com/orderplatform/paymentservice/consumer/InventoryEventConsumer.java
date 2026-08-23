package com.orderplatform.paymentservice.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderplatform.paymentservice.event.InventoryReservedEvent;
import com.orderplatform.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    @KafkaListener(topics = "inventory.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(String rawJson) {
        try {
            JsonNode node = objectMapper.readTree(rawJson);
            boolean isReservation = node.has("customerId") && node.has("totalAmount");

            if (!isReservation) {
                log.info("Ignoring inventory failure event for order {} — no payment attempt needed",
                        node.path("orderId").asText());
                return;
            }

            List<InventoryReservedEvent.ReservedItem> items = new ArrayList<>();
            for (JsonNode itemNode : node.get("items")) {
                items.add(new InventoryReservedEvent.ReservedItem(
                        itemNode.get("productId").asText(),
                        itemNode.get("quantity").asInt()
                ));
            }

            InventoryReservedEvent event = new InventoryReservedEvent(
                    UUID.fromString(node.get("orderId").asText()),
                    node.get("customerId").asText(),
                    items,
                    new BigDecimal(node.get("totalAmount").asText())
            );

            log.info("Received InventoryReservedEvent for order {} ({} item(s))", event.orderId(), items.size());
            paymentService.handleInventoryReserved(event);

        } catch (Exception e) {
            log.error("Failed to process inventory event: {}", e.getMessage(), e);
        }
    }
}