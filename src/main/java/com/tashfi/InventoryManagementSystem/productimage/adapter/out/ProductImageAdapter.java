package com.tashfi.InventoryManagementSystem.productimage.adapter.out;

import com.tashfi.InventoryManagementSystem.productimage.adapter.out.repository.ProductImageRepository;
import com.tashfi.InventoryManagementSystem.productimage.application.port.out.ProductImagePersistencePort;
import com.tashfi.InventoryManagementSystem.productimage.domain.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductImageAdapter implements ProductImagePersistencePort {

    private final ProductImageRepository productImageRepository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<ProductImage> save(ProductImage productImage) {
        LocalDateTime now = productImage.getCreatedAt() != null
                ? productImage.getCreatedAt() : LocalDateTime.now();

        return databaseClient.sql("""
                        INSERT INTO product_image (product_id, image_url, created_at)
                        VALUES (:productId, :imageUrl, :createdAt)
                        RETURNING *
                        """)
                .bind("productId", productImage.getProductId())
                .bind("imageUrl", productImage.getImageUrl())
                .bind("createdAt", now)
                .fetch()
                .one()
                .map(this::fromRow);
    }

    @Override
    public Flux<ProductImage> findAllByProductId(UUID productId) {
        return databaseClient.sql("""
                        SELECT * FROM product_image
                        WHERE product_id = :productId
                        ORDER BY created_at ASC
                        """)
                .bind("productId", productId)
                .fetch()
                .all()
                .map(this::fromRow);
    }

    @Override
    public Mono<ProductImage> findByIdAndProductId(UUID imageId, UUID productId) {
        return databaseClient.sql("""
                        SELECT * FROM product_image
                        WHERE id = :id AND product_id = :productId
                        """)
                .bind("id", imageId)
                .bind("productId", productId)
                .fetch()
                .one()
                .map(this::fromRow);
    }

    @Override
    public Mono<ProductImage> update(UUID imageId, UUID productId, String newImageUrl) {
        return databaseClient.sql("""
                        UPDATE product_image
                        SET image_url = :imageUrl
                        WHERE id = :id AND product_id = :productId
                        RETURNING *
                        """)
                .bind("imageUrl", newImageUrl)
                .bind("id", imageId)
                .bind("productId", productId)
                .fetch()
                .one()
                .map(this::fromRow);
    }

    @Override
    public Mono<Void> deleteByIdAndProductId(UUID imageId, UUID productId) {
        return productImageRepository.deleteByIdAndProductId(imageId, productId);
    }

    // ----------- Private helpers -----------------------------------------------------------

    private ProductImage fromRow(java.util.Map<String, Object> row) {
        return ProductImage.builder()
                .id((UUID) row.get("id"))
                .productId((UUID) row.get("product_id"))
                .imageUrl((String) row.get("image_url"))
                .createdAt((LocalDateTime) row.get("created_at"))
                .build();
    }
}