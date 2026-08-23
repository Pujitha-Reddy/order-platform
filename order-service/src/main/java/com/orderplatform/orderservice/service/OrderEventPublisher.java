package com.orderplatform.orderservice.service;

import com.orderplatform.orderservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "order.events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.orderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCreatedEvent for order {}", event.orderId(), ex);
                    } else {
                        log.info("Published OrderCreatedEvent for order {} to partition {}",
                                event.orderId(), result.getRecordMetadata().partition());
                    }
                });
    }
}