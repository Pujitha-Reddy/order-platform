package com.orderplatform.inventoryservice.controller;

import com.orderplatform.inventoryservice.dto.ProductResponse;
import com.orderplatform.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;

    @GetMapping
    public List<ProductResponse> listProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::from)
                .toList();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable String productId) {
        return productRepository.findById(productId)
                .map(ProductResponse::from)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new NoSuchElementException("Product not found: " + productId));
    }
}