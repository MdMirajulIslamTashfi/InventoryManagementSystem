package com.tashfi.InventoryManagementSystem.productcategory.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.CategoryNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateProductException;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.CategoryUseCase;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.request.CategoryRequestDto;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response.CategoryResponseDto;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response.CategorySingleResponseDto;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.out.CategoryPersistencePort;
import com.tashfi.InventoryManagementSystem.productcategory.domain.ProductCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryPersistencePort categoryPersistencePort;

    @Override
    public Mono<CategoryResponseDto> findAllCategories() {
        log.info("Fetching all categories");
        return categoryPersistencePort.findAll()
                .collectList()
                .map(list -> {
                    log.info("Fetched {} categories successfully", list.size());
                    return CategoryResponseDto.builder()
                            .message("Categories fetched successfully")
                            .totalRecords(list.size())
                            .categoryData(list)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching all categories: {}", ex.getMessage(), ex));
    }

    @Override
    public Mono<CategorySingleResponseDto> searchCategoryByName(String name) {
        log.info("Searching category by name: {}", name);
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Category not found for name: {}", name);
                    return Mono.error(new CategoryNotFoundException("Category not found: " + name));
                }))
                .map(cat -> {
                    log.info("Category found successfully for name: {}", name);
                    return CategorySingleResponseDto.builder()
                            .message("Category found")
                            .categoryData(cat)
                            .build();
                })
                .doOnError(ex -> log.error("Error while searching category by name [{}]: {}",
                        name, ex.getMessage(), ex));
    }

    @Override
    public Mono<CategorySingleResponseDto> createCategory(CategoryRequestDto request) {
        log.info("Creating category with name: {}", request.getName());
        return ValidationUtil.validateName(request.getName())
                .then(categoryPersistencePort.existsByName(request.getName()))
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Create failed - duplicate category name: {}", request.getName());
                        return Mono.error(new DuplicateProductException("Category already exists: " + request.getName()));
                    }

                    return categoryPersistencePort.save(ProductCategory.builder()
                            .name(request.getName())
                            .description(request.getDescription())
                            .createdAt(LocalDateTime.now())
                            .build());
                })
                .map(saved -> {
                    log.info("Category created successfully with name: {}", saved.getName());
                    return CategorySingleResponseDto.builder()
                            .message("Category created successfully")
                            .categoryData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while creating category [{}]: {}",
                        request.getName(), ex.getMessage(), ex));
    }

    @Override
    public Mono<CategorySingleResponseDto> updateCategory(String name, CategoryRequestDto request) {
        log.info("Updating category with name: {}", name);
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Update failed - category not found: {}", name);
                    return Mono.error(new CategoryNotFoundException("Category not found: " + name));
                }))
                .flatMap(existing -> categoryPersistencePort.update(name, ProductCategory.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .build()))
                .map(saved -> {
                    log.info("Category updated successfully - old name: {}, new name: {}", name, saved.getName());
                    return CategorySingleResponseDto.builder()
                            .message("Category updated successfully")
                            .categoryData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while updating category [{}]: {}",
                        name, ex.getMessage(), ex));
    }

    @Override
    public Mono<Void> deleteCategory(String name) {
        log.info("Deleting category with name: {}", name);
        return categoryPersistencePort.findByName(name)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Delete failed - category not found: {}", name);
                    return Mono.error(new CategoryNotFoundException("Category not found: " + name));
                }))
                .flatMap(existing -> categoryPersistencePort.deleteByName(name)
                        .doOnSuccess(v -> log.info("Category deleted successfully with name: {}", name)))
                .doOnError(ex -> log.error("Error while deleting category [{}]: {}",
                        name, ex.getMessage(), ex));
    }
}