package com.tashfi.InventoryManagementSystem.product.adapter.out;

import com.tashfi.InventoryManagementSystem.product.adapter.out.entity.ProductEntity;
import com.tashfi.InventoryManagementSystem.productcategory.adapter.out.repository.ProductCategoryRepository;
import com.tashfi.InventoryManagementSystem.product.adapter.out.repository.ProductRepository;
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductAdapter implements ProductPersistencePort {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll().flatMap(this::enrichWithCategoryName);
    }

    @Override
    public Mono<Product> findById(UUID id) {
        // findById is inherited from R2dbcRepository<ProductEntity, UUID> — no custom query needed
        return productRepository.findById(id).flatMap(this::enrichWithCategoryName);
    }

    @Override
    public Flux<Product> searchByName(String name) {
        return productRepository.searchByNameContaining(name).flatMap(this::enrichWithCategoryName);
    }

    @Override
    public Mono<Boolean> existsBySku(String sku) {
        return productRepository.existsBySku(sku);
    }

    @Override
    public Mono<Product> save(Product product) {
        return productRepository.save(toEntity(product)).flatMap(this::enrichWithCategoryName);
    }

    @Override
    public Mono<Product> update(UUID id, Product product) {
        return productRepository.findById(id)
                .flatMap(existing -> {
                    existing.setCategoryId(product.getCategoryId() != null ? product.getCategoryId() : existing.getCategoryId());
                    existing.setName(product.getName() != null ? product.getName() : existing.getName());
                    existing.setDescription(product.getDescription() != null ? product.getDescription() : existing.getDescription());
                    existing.setQuantity(product.getQuantity() != null ? product.getQuantity() : existing.getQuantity());
                    existing.setPrice(product.getPrice() != null ? product.getPrice() : existing.getPrice());
                    existing.setSku(product.getSku() != null ? product.getSku() : existing.getSku());
                    existing.setStatus(product.getStatus() != null ? product.getStatus() : existing.getStatus());
                    existing.setUpdatedAt(product.getUpdatedAt());
                    return productRepository.save(existing);
                })
                .flatMap(this::enrichWithCategoryName);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        // deleteById is inherited from R2dbcRepository<ProductEntity, UUID>
        return productRepository.deleteById(id);
    }

    private Mono<Product> enrichWithCategoryName(ProductEntity entity) {
        return categoryRepository.findById(entity.getCategoryId())
                .map(cat -> toDomain(entity, cat.getName()))
                .defaultIfEmpty(toDomain(entity, "Unknown"));
    }

    private Product toDomain(ProductEntity e, String categoryName) {
        return Product.builder()
                .id(e.getId())
                .categoryId(e.getCategoryId())
                .categoryName(categoryName)
                .name(e.getName())
                .description(e.getDescription())
                .quantity(e.getQuantity())
                .price(e.getPrice())
                .sku(e.getSku())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    private ProductEntity toEntity(Product p) {
        return ProductEntity.builder()
                .id(p.getId())
                .categoryId(p.getCategoryId())
                .name(p.getName())
                .description(p.getDescription())
                .quantity(p.getQuantity())
                .price(p.getPrice())
                .sku(p.getSku())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}