package com.orderplatform.orderservice.service;
import java.util.List;
import com.orderplatform.orderservice.dto.CreateOrderRequest;
import com.orderplatform.orderservice.dto.OrderResponse;
import com.orderplatform.orderservice.event.OrderCreatedEvent;
import com.orderplatform.orderservice.model.Order;
import com.orderplatform.orderservice.model.OrderItem;
import com.orderplatform.orderservice.model.OrderStatus;
import com.orderplatform.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher eventPublisher;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UUID userId) {
        Order order = Order.builder()
                .customerId(request.customerId())
                .userId(userId)
                .status(OrderStatus.CREATED)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.OrderItemRequest itemReq : request.items()) {
            OrderItem item = OrderItem.builder()
                    .productId(itemReq.productId())
                    .quantity(itemReq.quantity())
                    .unitPrice(itemReq.unitPrice())
                    .build();
            order.addItem(item);
            total = total.add(itemReq.unitPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        List<OrderCreatedEvent.OrderItemPayload> eventItems = saved.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItemPayload(i.getProductId(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        eventPublisher.publishOrderCreated(new OrderCreatedEvent(
                saved.getId(),
                saved.getCustomerId(),
                eventItems,
                saved.getTotalAmount(),
                saved.getCreatedAt()
        ));

        return OrderResponse.from(saved);
    }

    public OrderResponse getOrder(UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Order not found: " + id));
        return OrderResponse.from(order);
    }
    public List<OrderResponse> getOrdersForUser(UUID userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public void updateStatus(UUID orderId, OrderStatus newStatus) {
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            order.setStatus(newStatus);
            orderRepository.save(order);
        }, () -> log.warn("Received status update for unknown order {}", orderId));
    }
}