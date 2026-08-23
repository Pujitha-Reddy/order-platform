package com.orderplatform.orderservice.controller;

import com.orderplatform.orderservice.dto.CreateOrderRequest;
import com.orderplatform.orderservice.dto.OrderResponse;
import com.orderplatform.orderservice.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        OrderResponse response = orderService.createOrder(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrder(id));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<OrderResponse>> getMyOrders(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(orderService.getOrdersForUser(userId));
    }
}