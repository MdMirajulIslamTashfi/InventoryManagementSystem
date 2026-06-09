package com.tashfi.InventoryManagementSystem.product.adapter.out.repository;

import com.tashfi.InventoryManagementSystem.product.adapter.out.entity.ProductEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository extends R2dbcRepository<ProductEntity, UUID> {
    Mono<ProductEntity> findByName(String name);
    Mono<Boolean> existsByName(String name);
    Mono<Boolean> existsBySku(String sku);
    Mono<Void> deleteByName(String name);

    @Query("SELECT * FROM product WHERE LOWER(name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Flux<ProductEntity> searchByNameContaining(String name);
}