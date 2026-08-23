package com.orderplatform.inventoryservice.dto;

import com.orderplatform.inventoryservice.model.Product;
import java.math.BigDecimal;

public record ProductResponse(
        String productId, String name, String description, BigDecimal price,
        String imageUrl, int availableQuantity, Double rating, Integer reviewCount, String category
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getProductId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getImageUrl(), product.getAvailableQuantity(), product.getRating(),
                product.getReviewCount(), product.getCategory()
        );
    }
}