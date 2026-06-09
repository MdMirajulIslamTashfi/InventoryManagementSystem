package com.tashfi.InventoryManagementSystem.product.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.CategoryNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateProductException;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.product.application.port.in.CategoryUseCase;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.CategoryRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.CategoryResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.CategorySingleResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.out.CategoryPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.ProductCategory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CategoryService implements CategoryUseCase {

    private final CategoryPersistencePort categoryPersistencePort;

    public CategoryService(CategoryPersistencePort categoryPersistencePort) {
        this.categoryPersistencePort = categoryPersistencePort;
    }

    @Override
    public Mono<CategoryResponseDto> findAllCategories() {
        return categoryPersistencePort.findAll()
                .collectList()
                .map(list -> CategoryResponseDto.builder()
                        .message("Categories fetched successfully")
                        .totalRecords(list.size())
                        .categoryData(list)
                        .build());
    }

    @Override
    public Mono<CategorySingleResponseDto> searchCategoryByName(String name) {
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException("Category not found: " + name)))
                .map(cat -> CategorySingleResponseDto.builder()
                        .message("Category found")
                        .categoryData(cat)
                        .build());
    }

    @Override
    public Mono<CategorySingleResponseDto> createCategory(CategoryRequestDto request) {
        return ValidationUtil.validateName(request.getName())
                .then(categoryPersistencePort.existsByName(request.getName()))
                .flatMap(exists -> {
                    if (exists)
                        return Mono.error(new DuplicateProductException("Category already exists: " + request.getName()));

                    return categoryPersistencePort.save(ProductCategory.builder()
                            .name(request.getName())
                            .description(request.getDescription())
                            .createdAt(LocalDateTime.now())
                            .build());
                })
                .map(saved -> CategorySingleResponseDto.builder()
                        .message("Category created successfully")
                        .categoryData(saved)
                        .build());
    }

    @Override
    public Mono<CategorySingleResponseDto> updateCategory(String name, CategoryRequestDto request) {
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException("Category not found: " + name)))
                .flatMap(existing -> categoryPersistencePort.update(name, ProductCategory.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .build()))
                .map(saved -> CategorySingleResponseDto.builder()
                        .message("Category updated successfully")
                        .categoryData(saved)
                        .build());
    }

    @Override
    public Mono<Void> deleteCategory(String name) {
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new CategoryNotFoundException("Category not found: " + name)))
                .flatMap(existing -> categoryPersistencePort.deleteByName(name));
    }
}