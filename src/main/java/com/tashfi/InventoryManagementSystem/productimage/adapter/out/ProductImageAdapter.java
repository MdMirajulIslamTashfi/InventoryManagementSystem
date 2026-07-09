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
import java.util.Map;
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
                        INSERT INTO product_image
                            (product_id, product_sku, product_name, image_url, image_name,
                             is_thumbnail, thumbnail_url, created_at, created_by)
                        VALUES
                            (:productId, :productSku, :productName, :imageUrl, :imageName,
                             :isThumbnail, :thumbnailUrl, :createdAt, :createdBy)
                        RETURNING *
                        """)
                .bind("productId", productImage.getProductId())
                .bind("productSku", productImage.getProductSku() != null ? productImage.getProductSku() : "")
                .bind("productName", productImage.getProductName() != null ? productImage.getProductName() : "")
                .bind("imageUrl", productImage.getImageUrl() != null ? productImage.getImageUrl() : "")
                .bind("imageName", productImage.getImageName() != null ? productImage.getImageName() : "")
                .bind("isThumbnail", productImage.getIsThumbnail() != null ? productImage.getIsThumbnail() : false)
                .bind("thumbnailUrl", productImage.getThumbnailUrl() != null ? productImage.getThumbnailUrl() : "")
                .bind("createdAt", now)
                .bind("createdBy", productImage.getCreatedBy() != null ? productImage.getCreatedBy() : "SYSTEM")
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
    public Mono<ProductImage> findById(UUID imageId) {
        return databaseClient.sql("""
                        SELECT * FROM product_image
                        WHERE id = :id
                        """)
                .bind("id", imageId)
                .fetch()
                .one()
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
    public Mono<ProductImage> update(ProductImage productImage) {
        return databaseClient.sql("""
                        UPDATE product_image
                        SET image_url     = :imageUrl,
                            image_name    = :imageName,
                            is_thumbnail  = :isThumbnail,
                            thumbnail_url = :thumbnailUrl,
                            updated_at    = :updatedAt,
                            updated_by    = :updatedBy
                        WHERE id = :id AND product_id = :productId
                        RETURNING *
                        """)
                .bind("imageUrl", productImage.getImageUrl() != null ? productImage.getImageUrl() : "")
                .bind("imageName", productImage.getImageName() != null ? productImage.getImageName() : "")
                .bind("isThumbnail", productImage.getIsThumbnail() != null ? productImage.getIsThumbnail() : false)
                .bind("thumbnailUrl", productImage.getThumbnailUrl() != null ? productImage.getThumbnailUrl() : "")
                .bind("updatedAt", LocalDateTime.now())
                .bind("updatedBy", productImage.getUpdatedBy() != null ? productImage.getUpdatedBy() : "SYSTEM")
                .bind("id", productImage.getId())
                .bind("productId", productImage.getProductId())
                .fetch()
                .one()
                .map(this::fromRow);
    }

    @Override
    public Mono<Void> deleteByIdAndProductId(UUID imageId, UUID productId) {
        return productImageRepository.deleteByIdAndProductId(imageId, productId);
    }

    @Override
    public Mono<Void> deleteAllByProductId(UUID productId) {
        return productImageRepository.deleteAllByProductId(productId);
    }

    // ----------- Private helpers -----------------------------------------------------------

    private ProductImage fromRow(Map<String, Object> row) {
        return ProductImage.builder()
                .id((UUID) row.get("id"))
                .productId((UUID) row.get("product_id"))
                .productSku((String) row.get("product_sku"))
                .productName((String) row.get("product_name"))
                .imageUrl((String) row.get("image_url"))
                .imageName((String) row.get("image_name"))
                .isThumbnail((Boolean) row.get("is_thumbnail"))
                .thumbnailUrl((String) row.get("thumbnail_url"))
                .createdAt((LocalDateTime) row.get("created_at"))
                .createdBy((String) row.get("created_by"))
                .updatedAt((LocalDateTime) row.get("updated_at"))
                .updatedBy((String) row.get("updated_by"))
                .build();
    }
}
