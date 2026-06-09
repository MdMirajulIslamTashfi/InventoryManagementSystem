package com.tashfi.InventoryManagementSystem.product.application.port.out;

import com.tashfi.InventoryManagementSystem.product.domain.ProductCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryPersistencePort {
    Flux<ProductCategory> findAll();
    Mono<ProductCategory> findByName(String name);
    Mono<Boolean> existsByName(String name);
    Mono<ProductCategory> save(ProductCategory category);
    Mono<ProductCategory> update(String name, ProductCategory category);
    Mono<Void> deleteByName(String name);
}