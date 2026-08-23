package com.orderplatform.inventoryservice.consumer;

import com.orderplatform.inventoryservice.event.OrderCreatedEvent;
import com.orderplatform.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order.events", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order {}", event.orderId());
        inventoryService.handleOrderCreated(event);
    }
}