package com.tashfi.InventoryManagementSystem.product.application.service;

import com.tashfi.InventoryManagementSystem.content.application.port.out.ContentServiceClientPort;
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
import com.tashfi.InventoryManagementSystem.productcategory.application.port.out.CategoryPersistencePort;
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.Product;
import com.tashfi.InventoryManagementSystem.productcategory.domain.ProductCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final CategoryPersistencePort categoryPersistencePort;

    private final ObjectProvider<ContentServiceClientPort> contentServiceClientPort;

    @Override
    public Mono<ProductResponseDto> findAllProducts() {
        log.info("Fetching all products");
        return productPersistencePort.findAll()
                .collectList()
                .map(list -> {
                    log.info("Fetched {} products successfully", list.size());
                    return ProductResponseDto.builder()
                            .message("Products fetched successfully")
                            .totalRecords(list.size())
                            .productData(list)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching all products: {}", ex.getMessage(), ex));
    }

    @Override
    public Mono<ProductResponseDto> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);
        return productPersistencePort.searchByName(name)
                .collectList()
                .flatMap(list -> {
                    if (list.isEmpty()) {
                        log.warn("No products found matching name: {}", name);
                        return Mono.error(new ProductNotFoundException("No products found matching: " + name));
                    }
                    log.info("Search for '{}' returned {} product(s)", name, list.size());
                    return Mono.just(ProductResponseDto.builder()
                            .message("Products found")
                            .totalRecords(list.size())
                            .productData(list)
                            .build());
                })
                .doOnError(ex -> log.error("Error while searching products by name [{}]: {}",
                        name, ex.getMessage(), ex));
    }

    @Override
    public Mono<ProductSingleResponseDto> findProductById(UUID id) {
        log.info("Fetching product by id: {}", id);
        return productPersistencePort.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Product not found for id: {}", id);
                    return Mono.error(new ProductNotFoundException("Product not found with id: " + id));
                }))
                .map(product -> {
                    log.info("Product fetched successfully for id: {}", id);
                    return ProductSingleResponseDto.builder()
                            .message("Product fetched successfully")
                            .productData(product)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching product by id [{}]: {}",
                        id, ex.getMessage(), ex));
    }

    @Override
    public Mono<ProductSingleResponseDto> createProduct(ProductRequestDto request) {
        log.info("Creating product with name: {}, sku: {}", request.getName(), request.getSku());
        return ValidationUtil.validateInput(request.getName())
                .then(categoryPersistencePort.findByName(request.getCategoryName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Create failed - category not found: {}", request.getCategoryName());
                    return Mono.error(new CategoryNotFoundException("Category not found: " + request.getCategoryName()));
                }))
                .flatMap(category -> {
                    Mono<Boolean> skuCheck = (request.getSku() != null && !request.getSku().isBlank())
                            ? productPersistencePort.existsBySku(request.getSku())
                            : Mono.just(false);

                    return skuCheck.flatMap(skuExists -> {
                        if (skuExists) {
                            log.warn("Create failed - duplicate SKU: {}", request.getSku());
                            return Mono.error(new DuplicateProductException("SKU already exists: " + request.getSku()));
                        }

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
                .map(saved -> {
                    log.info("Product created successfully with id: {}", saved.getId());
                    return ProductSingleResponseDto.builder()
                            .message("Product created successfully")
                            .productData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while creating product [{}]: {}",
                        request.getName(), ex.getMessage(), ex));
    }

    @Override
    public Mono<ProductSingleResponseDto> updateProduct(UUID id, ProductUpdateRequestDto request) {
        log.info("Updating product with id: {}", id);
        return productPersistencePort.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Update failed - product not found for id: {}", id);
                    return Mono.error(new ProductNotFoundException("Product not found with id: " + id));
                }))
                .flatMap(existing -> {
                    Mono<Boolean> categoryCheck = (request.getCategoryName() != null)
                            ? categoryPersistencePort.existsByName(request.getCategoryName())
                            : Mono.just(true);

                    return categoryCheck.flatMap(categoryExists -> {
                        if (!categoryExists) {
                            log.warn("Update failed - category not found: {}", request.getCategoryName());
                            return Mono.error(new CategoryNotFoundException(
                                    "Category not found: " + request.getCategoryName()));
                        }

                        Mono<UUID> resolvedCategoryId = (request.getCategoryName() != null)
                                ? categoryPersistencePort.findByName(request.getCategoryName())
                                .map(ProductCategory::getId)
                                : Mono.just(existing.getCategoryId());

                        return resolvedCategoryId.flatMap(categoryId ->
                                productPersistencePort.update(id, Product.builder()
                                        .categoryId(categoryId)
                                        .name(request.getName() != null ? request.getName() : existing.getName())
                                        .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                                        .quantity(request.getQuantity() != null ? request.getQuantity() : existing.getQuantity())
                                        .price(request.getPrice() != null ? request.getPrice() : existing.getPrice())
                                        .sku(request.getSku() != null ? request.getSku() : existing.getSku())
                                        .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                                        .updatedAt(LocalDateTime.now())
                                        .build()));
                    });
                })
                .map(saved -> {
                    log.info("Product updated successfully for id: {}", id);
                    return ProductSingleResponseDto.builder()
                            .message("Product updated successfully")
                            .productData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while updating product [{}]: {}",
                        id, ex.getMessage(), ex));
    }

    @Override
    public Mono<Void> deleteProduct(UUID id) {
        log.info("Deleting product with id: {}", id);
        return productPersistencePort.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Delete failed - product not found for id: {}", id);
                    return Mono.error(new ProductNotFoundException("Product not found with id: " + id));
                }))
                .flatMap(p -> productPersistencePort.deleteById(id)
                        .doOnSuccess(v -> log.info("Product deleted successfully for id: {}", id))
                        .then(cleanUpContentImages(p.getId())))
                .doOnError(ex -> log.error("Error while deleting product [{}]: {}",
                        id, ex.getMessage(), ex));
    }

    private Mono<Void> cleanUpContentImages(UUID productId) {
        ContentServiceClientPort client = contentServiceClientPort.getIfAvailable();
        if (client == null) {
            log.debug("Content profile not active - skipping CMS image cleanup for product id: {}", productId);
            return Mono.empty();
        }

        log.debug("Requesting CMS to clean up images for product id: {}", productId);
        return client.deleteAllByProductId(productId)
                .doOnError(e -> log.warn("CMS image cleanup failed for product id [{}] - continuing anyway: {}",
                        productId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }
}