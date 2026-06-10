package com.tashfi.InventoryManagementSystem.product.application.service;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import com.tashfi.InventoryManagementSystem.core.exception.CategoryNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateProductException;
import com.tashfi.InventoryManagementSystem.core.exception.ProductNotFoundException;
import com.tashfi.InventoryManagementSystem.core.storage.StorageService;
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
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final CategoryPersistencePort categoryPersistencePort;
    private final StorageService storageService;

    public ProductService(ProductPersistencePort productPersistencePort,
                          CategoryPersistencePort categoryPersistencePort,
                          StorageService storageService) {
        this.productPersistencePort = productPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.storageService = storageService;
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
    public Mono<ProductSingleResponseDto> createProduct(ProductRequestDto request, List<MultipartFile> images) {
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

                        String identifier = (request.getSku() != null && !request.getSku().isBlank())
                                ? request.getSku()
                                : request.getName();

                        List<MultipartFile> validImages = images != null
                                ? images.stream().filter(f -> f != null && !f.isEmpty()).toList()
                                : List.of();

                        Mono<String> imagesMono = validImages.isEmpty()
                                ? Mono.just("")
                                : storageService.uploadImages(validImages, identifier)
                                .map(paths -> String.join(",", paths));

                        return imagesMono.flatMap(imagesValue ->
                                productPersistencePort.save(Product.builder()
                                        .categoryId(category.getId())
                                        .name(request.getName())
                                        .description(request.getDescription())
                                        .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                                        .price(request.getPrice())
                                        .sku(request.getSku())
                                        .status(request.getStatus() != null ? request.getStatus() : ProductStatus.ACTIVE)
                                        .images(imagesValue.isBlank() ? null : imagesValue)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build())
                        );
                    });
                })
                .map(saved -> ProductSingleResponseDto.builder()
                        .message("Product created successfully")
                        .productData(saved)
                        .build());
    }

    @Override
    public Mono<ProductSingleResponseDto> updateProduct(String name, ProductUpdateRequestDto request, List<MultipartFile> newImages) {
        return productPersistencePort.findByName(name)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found: " + name)))
                .flatMap(existing -> {
                    Mono<Boolean> categoryCheck = (request.getCategoryName() != null)
                            ? categoryPersistencePort.existsByName(request.getCategoryName())
                            : Mono.just(true);

                    return categoryCheck.flatMap(categoryExists -> {
                        if (!categoryExists)
                            return Mono.error(new CategoryNotFoundException(
                                    "Category not found: " + request.getCategoryName()));

                        Mono<UUID> resolvedCategoryId = (request.getCategoryName() != null)
                                ? categoryPersistencePort.findByName(request.getCategoryName())
                                .map(c -> c.getId())
                                : Mono.just(existing.getCategoryId());

                        return resolvedCategoryId.flatMap(categoryId -> {
                            // identifier for new image filenames
                            String identifier = (request.getSku() != null && !request.getSku().isBlank())
                                    ? request.getSku()
                                    : (existing.getSku() != null && !existing.getSku().isBlank())
                                      ? existing.getSku()
                                      : existing.getName();

                            // upload new images if any provided
                            List<MultipartFile> validImages = newImages != null
                                    ? newImages.stream()
                                    .filter(f -> f != null && !f.isEmpty())
                                    .toList()
                                    : List.of();

                            Mono<String> mergedImagesMono = validImages.isEmpty()
                                    ? Mono.just(existing.getImages() != null ? existing.getImages() : "")
                                    : storageService.uploadImages(validImages, identifier)
                                    .map(newPaths -> {
                                        String newPathsStr = String.join(",", newPaths);
                                        // append new paths to existing ones
                                        if (existing.getImages() != null && !existing.getImages().isBlank())
                                            return existing.getImages() + "," + newPathsStr;
                                        return newPathsStr;
                                    });

                            return mergedImagesMono.flatMap(mergedImages ->
                                    productPersistencePort.update(name, Product.builder()
                                            .categoryId(categoryId)
                                            .name(request.getName() != null ? request.getName() : existing.getName())
                                            .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                                            .quantity(request.getQuantity() != null ? request.getQuantity() : existing.getQuantity())
                                            .price(request.getPrice() != null ? request.getPrice() : existing.getPrice())
                                            .sku(request.getSku() != null ? request.getSku() : existing.getSku())
                                            .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                                            .images(mergedImages.isBlank() ? null : mergedImages)
                                            .updatedAt(LocalDateTime.now())
                                            .build())
                            );
                        });
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