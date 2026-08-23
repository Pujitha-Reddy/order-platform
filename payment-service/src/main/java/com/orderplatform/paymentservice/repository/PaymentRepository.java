package com.orderplatform.paymentservice.repository;

import com.orderplatform.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByOrderId(UUID orderId);
}