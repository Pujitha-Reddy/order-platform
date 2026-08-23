package com.orderplatform.inventoryservice.repository;

import com.orderplatform.inventoryservice.model.ProcessedOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedOrderRepository extends JpaRepository<ProcessedOrder, UUID> {
}