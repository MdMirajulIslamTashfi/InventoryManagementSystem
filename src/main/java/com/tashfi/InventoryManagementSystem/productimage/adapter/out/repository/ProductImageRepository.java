package com.tashfi.InventoryManagementSystem.productimage.adapter.out.repository;

import com.tashfi.InventoryManagementSystem.productimage.adapter.out.entity.ProductImageEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductImageRepository extends R2dbcRepository<ProductImageEntity, UUID> {
    Flux<ProductImageEntity> findAllByProductId(UUID productId);
    Mono<ProductImageEntity> findByIdAndProductId(UUID id, UUID productId);
    Mono<Void> deleteByIdAndProductId(UUID id, UUID productId);
    Mono<Void> deleteAllByProductId(UUID productId);
}
