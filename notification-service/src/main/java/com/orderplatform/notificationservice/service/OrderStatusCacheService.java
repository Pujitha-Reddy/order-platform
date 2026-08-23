package com.orderplatform.notificationservice.service;

import com.orderplatform.notificationservice.model.OrderStatusUpdate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderStatusCacheService {

    private static final String KEY_PREFIX = "order-status:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, OrderStatusUpdate> redisTemplate;

    public void save(OrderStatusUpdate update) {
        redisTemplate.opsForValue().set(KEY_PREFIX + update.orderId(), update, TTL);
    }

    public Optional<OrderStatusUpdate> get(String orderId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + orderId));
    }
}