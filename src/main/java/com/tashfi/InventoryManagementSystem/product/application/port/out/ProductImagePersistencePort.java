package com.tashfi.InventoryManagementSystem.product.application.port.out;

import com.tashfi.InventoryManagementSystem.product.domain.ProductImage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductImagePersistencePort {
    // Save a single image record linked to a product
    Mono<ProductImage> save(ProductImage productImage);

    // Get all images belonging to a product
    Flux<ProductImage> findAllByProductId(UUID productId);

    // Get a specific image by its own ID and its product's ID
    Mono<ProductImage> findByIdAndProductId(UUID imageId, UUID productId);

    // Update the URL of a specific image (replace the file)
    Mono<ProductImage> update(UUID imageId, UUID productId, String newImageUrl);

    // Delete a specific image by its ID and product ID
    Mono<Void> deleteByIdAndProductId(UUID imageId, UUID productId);
}
