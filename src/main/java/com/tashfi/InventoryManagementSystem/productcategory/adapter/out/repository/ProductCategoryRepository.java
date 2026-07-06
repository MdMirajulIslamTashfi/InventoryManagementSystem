package com.tashfi.InventoryManagementSystem.productcategory.adapter.out.repository;

import com.tashfi.InventoryManagementSystem.productcategory.adapter.out.entity.ProductCategoryEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductCategoryRepository extends R2dbcRepository<ProductCategoryEntity, UUID> {
    Mono<Boolean> existsByName(String name);
    Mono<ProductCategoryEntity> findByName(String name);
    Mono<Void> deleteByName(String name);
}