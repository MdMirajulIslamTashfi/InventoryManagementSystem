package com.tashfi.InventoryManagementSystem.product.application.port.out;

import com.tashfi.InventoryManagementSystem.product.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductPersistencePort {
    Flux<Product> findAll();
    Mono<Product> findById(UUID id);
    Flux<Product> searchByName(String name);
    Mono<Boolean> existsBySku(String sku);
    Mono<Product> save(Product product);
    Mono<Product> update(UUID id, Product product);
    Mono<Void> deleteById(UUID id);
}