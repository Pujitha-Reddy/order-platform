package com.orderplatform.inventoryservice.service;

import com.orderplatform.inventoryservice.event.InventoryFailedEvent;
import com.orderplatform.inventoryservice.event.InventoryReservedEvent;
import com.orderplatform.inventoryservice.event.OrderCreatedEvent;
import com.orderplatform.inventoryservice.model.ProcessedOrder;
import com.orderplatform.inventoryservice.model.Product;
import com.orderplatform.inventoryservice.repository.ProcessedOrderRepository;
import com.orderplatform.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private static final String INVENTORY_TOPIC = "inventory.events";

    private final ProductRepository productRepository;
    private final ProcessedOrderRepository processedOrderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void handleOrderCreated(OrderCreatedEvent event) {

        if (processedOrderRepository.existsById(event.orderId())) {
            log.warn("Order {} already processed — skipping duplicate delivery", event.orderId());
            return;
        }

        // Validate every item BEFORE reserving anything — all-or-nothing.
        for (OrderCreatedEvent.OrderItemPayload item : event.items()) {
            Optional<Product> productOpt = productRepository.findById(item.productId());

            if (productOpt.isEmpty()) {
                failOrder(event, "Product not found: " + item.productId());
                return;
            }
            if (productOpt.get().getAvailableQuantity() < item.quantity()) {
                failOrder(event, "Insufficient stock for product: " + item.productId());
                return;
            }
        }

        // All items validated — now actually reserve. Any concurrent-update
        // conflict here throws, rolling back the whole transaction, including
        // any earlier items already saved in this same loop.
        List<InventoryReservedEvent.ReservedItem> reserved = new ArrayList<>();
        for (OrderCreatedEvent.OrderItemPayload item : event.items()) {
            Product product = productRepository.findById(item.productId()).orElseThrow();
            product.setAvailableQuantity(product.getAvailableQuantity() - item.quantity());
            productRepository.save(product);
            reserved.add(new InventoryReservedEvent.ReservedItem(item.productId(), item.quantity()));
        }

        markProcessed(event.orderId());

        kafkaTemplate.send(INVENTORY_TOPIC, event.orderId().toString(),
                new InventoryReservedEvent(event.orderId(), event.customerId(), reserved, event.totalAmount()));

        log.info("Reserved {} item(s) for order {}", reserved.size(), event.orderId());
    }

    private void failOrder(OrderCreatedEvent event, String reason) {
        markProcessed(event.orderId());
        kafkaTemplate.send(INVENTORY_TOPIC, event.orderId().toString(),
                new InventoryFailedEvent(event.orderId(), reason));
        log.warn("Inventory reservation failed for order {}: {}", event.orderId(), reason);
    }

    private void markProcessed(java.util.UUID orderId) {
        processedOrderRepository.save(ProcessedOrder.builder()
                .orderId(orderId)
                .processedAt(Instant.now())
                .build());
    }
}