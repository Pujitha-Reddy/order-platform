package com.orderplatform.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedOrder {

    @Id
    private UUID orderId;

    @Column(nullable = false)
    private Instant processedAt;
}