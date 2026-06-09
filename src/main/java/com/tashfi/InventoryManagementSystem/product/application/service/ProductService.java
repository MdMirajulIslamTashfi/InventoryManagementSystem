package com.tashfi.InventoryManagementSystem.product.application.service;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import com.tashfi.InventoryManagementSystem.core.exception.CategoryNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateProductException;
import com.tashfi.InventoryManagementSystem.core.exception.ProductNotFoundException;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.product.application.port.in.ProductUseCase;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductSingleResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.out.CategoryPersistencePort;
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.Product;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final CategoryPersistencePort categoryPersistencePort;

    public ProductService(ProductPersistencePort productPersistencePort,
                          CategoryPersistencePort categoryPersistencePort) {
        this.productPersistencePort = productPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
    }

    @Override
    public Mono<ProductResponseDto> findAllProducts() {
        return productPersistencePort.findAll()
                .collectList()
                .map(list -> ProductResponseDto.builder()
                        .message("Products fetched successfully")
                        .totalRecords(list.size())
                        .productData(list)
                        .build());
    }

    @Override
    public Mono<ProductResponseDto> searchProductsByName(String name) {
        return productPersistencePort.searchByName(name)
                .collectList()
                .map(list -> ProductResponseDto.builder()
                        .message(list.isEmpty() ? "No products found" : "Products found")
                        .totalRecords(list.size())
                        .productData(list)
                        .build());
    }

    @Override
    public Mono<ProductSingleResponseDto> createProduct(ProductRequestDto request) {
        return ValidationUtil.validateInput(request.getName())
                .then(categoryPersistencePort.findByName(request.getCategoryName()))
                .switchIfEmpty(Mono.error(new CategoryNotFoundException("Category not found: " + request.getCategoryName())))
                .flatMap(category -> {
                    Mono<Boolean> skuCheck = (request.getSku() != null && !request.getSku().isBlank())
                            ? productPersistencePort.existsBySku(request.getSku())
                            : Mono.just(false);

                    return skuCheck.flatMap(skuExists -> {
                        if (skuExists)
                            return Mono.error(new DuplicateProductException("SKU already exists: " + request.getSku()));

                        return productPersistencePort.save(Product.builder()
                                .categoryId(category.getId())
                                .name(request.getName())
                                .description(request.getDescription())
                                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                                .price(request.getPrice())
                                .sku(request.getSku())
                                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build());
                    });
                })
                .map(saved -> ProductSingleResponseDto.builder()
                        .message("Product created successfully")
                        .productData(saved)
                        .build());
    }

    @Override
    public Mono<ProductSingleResponseDto> updateProduct(String name, ProductUpdateRequestDto request) {
        return productPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found: " + name)))
                .flatMap(existing -> {
                    Mono<Boolean> categoryCheck = (request.getCategoryName() != null)
                            ? categoryPersistencePort.existsByName(request.getCategoryName())
                            : Mono.just(true);

                    return categoryCheck.flatMap(categoryExists -> {
                        if (!categoryExists)
                            return Mono.error(new CategoryNotFoundException("Category not found: " + request.getCategoryName()));

                        Mono<java.util.UUID> resolvedCategoryId = (request.getCategoryName() != null)
                                ? categoryPersistencePort.findByName(request.getCategoryName()).map(c -> c.getId())
                                : Mono.just(existing.getCategoryId());

                        return resolvedCategoryId.flatMap(categoryId ->
                                productPersistencePort.update(name, Product.builder()
                                        .categoryId(categoryId)
                                        .name(request.getName())
                                        .description(request.getDescription())
                                        .quantity(request.getQuantity())
                                        .price(request.getPrice())
                                        .sku(request.getSku())
                                        .status(request.getStatus())
                                        .updatedAt(LocalDateTime.now())
                                        .build())
                        );
                    });
                })
                .map(saved -> ProductSingleResponseDto.builder()
                        .message("Product updated successfully")
                        .productData(saved)
                        .build());
    }

    @Override
    public Mono<Void> deleteProduct(String name) {
        return productPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found: " + name)))
                .flatMap(p -> productPersistencePort.deleteByName(name));
    }
}