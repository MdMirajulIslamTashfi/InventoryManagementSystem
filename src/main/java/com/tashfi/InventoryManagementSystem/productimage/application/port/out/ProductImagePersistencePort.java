package com.tashfi.InventoryManagementSystem.productimage.application.port.out;

import com.tashfi.InventoryManagementSystem.productimage.domain.ProductImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductImagePersistencePort {
    Mono<ProductImage> save(ProductImage productImage);
    Flux<ProductImage> findAllByProductId(UUID productId);
    Mono<ProductImage> findById(UUID imageId);
    Mono<ProductImage> findByIdAndProductId(UUID imageId, UUID productId);
    Mono<ProductImage> update(ProductImage productImage);
    Mono<Void> deleteByIdAndProductId(UUID imageId, UUID productId);
    Mono<Void> deleteAllByProductId(UUID productId);
}
