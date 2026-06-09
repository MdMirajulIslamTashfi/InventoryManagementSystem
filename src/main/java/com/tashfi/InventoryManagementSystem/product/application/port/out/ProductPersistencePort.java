package com.tashfi.InventoryManagementSystem.product.application.port.out;

import com.tashfi.InventoryManagementSystem.product.domain.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductPersistencePort {
    Flux<Product> findAll();
    Mono<Product> findByName(String name);
    Flux<Product> searchByName(String name);
    Mono<Boolean> existsByName(String name);
    Mono<Boolean> existsBySku(String sku);
    Mono<Product> save(Product product);
    Mono<Product> update(String name, Product product);
    Mono<Void> deleteByName(String name);
}