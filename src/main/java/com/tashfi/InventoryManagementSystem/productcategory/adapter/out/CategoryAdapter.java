package com.tashfi.InventoryManagementSystem.productcategory.adapter.out;

import com.tashfi.InventoryManagementSystem.productcategory.adapter.out.entity.ProductCategoryEntity;
import com.tashfi.InventoryManagementSystem.productcategory.adapter.out.repository.ProductCategoryRepository;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.out.CategoryPersistencePort;
import com.tashfi.InventoryManagementSystem.productcategory.domain.ProductCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CategoryAdapter implements CategoryPersistencePort {

    private final ProductCategoryRepository categoryRepository;

    @Override
    public Flux<ProductCategory> findAll() {
        return categoryRepository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<ProductCategory> findByName(String name) {
        return categoryRepository.findByName(name).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    @Override
    public Mono<ProductCategory> save(ProductCategory category) {
        return categoryRepository.save(toEntity(category)).map(this::toDomain);
    }

    @Override
    public Mono<ProductCategory> update(String name, ProductCategory category) {
        return categoryRepository.findByName(name)
                .flatMap(existing -> {
                    existing.setName(category.getName() != null ? category.getName() : existing.getName());
                    existing.setDescription(category.getDescription() != null ? category.getDescription() : existing.getDescription());
                    return categoryRepository.save(existing);
                })
                .map(this::toDomain);
    }

    @Override
    public Mono<Void> deleteByName(String name) {
        return categoryRepository.deleteByName(name);
    }

    private ProductCategory toDomain(ProductCategoryEntity e) {
        return ProductCategory.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private ProductCategoryEntity toEntity(ProductCategory c) {
        return ProductCategoryEntity.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .createdAt(c.getCreatedAt())
                .build();
    }
}