package com.tashfi.InventoryManagementSystem.product.application.service;

import com.tashfi.InventoryManagementSystem.content.application.port.out.ContentServiceClientPort;
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
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductImagePersistencePort;
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.Product;
import com.tashfi.InventoryManagementSystem.product.domain.ProductCategory;
import com.tashfi.InventoryManagementSystem.product.domain.ProductImage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final CategoryPersistencePort categoryPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final StorageService storageService;
    // Optional — only present when the "content" profile is active.
    // Using ObjectProvider keeps ProductService startable on dev/local alone,
    // with no CMS bean and no CMS calls, while still letting us reach CMS
    // for cleanup when "content" is active.
    private final ObjectProvider<ContentServiceClientPort> contentServiceClientPort;

    public ProductService(ProductPersistencePort productPersistencePort,
                          CategoryPersistencePort categoryPersistencePort,
                          ProductImagePersistencePort productImagePersistencePort,
                          StorageService storageService,
                          ObjectProvider<ContentServiceClientPort> contentServiceClientPort) {
        this.productPersistencePort = productPersistencePort;
        this.categoryPersistencePort = categoryPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.storageService = storageService;
        this.contentServiceClientPort = contentServiceClientPort;
    }

    @Override
    public Mono<ProductResponseDto> findAllProducts() {
        return productPersistencePort.findAll()
                .flatMap(this::enrichWithImages)
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
                .flatMap(this::enrichWithImages)
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

                        // 1. Save the product first to get its generated UUID
                        Mono<Product> savedProductMono = productPersistencePort.save(Product.builder()
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

                        return savedProductMono.flatMap(savedProduct -> {
                            List<MultipartFile> validImages = images != null
                                    ? images.stream().filter(f -> f != null && !f.isEmpty()).toList()
                                    : List.of();

                            if (validImages.isEmpty()) {
                                // No images — return product with empty images list
                                savedProduct.setImages(List.of());
                                return Mono.just(savedProduct);
                            }

                            // 2. Upload images and save each one to product_image table
                            String identifier = (request.getSku() != null && !request.getSku().isBlank())
                                    ? request.getSku() : request.getName();

                            return storageService.uploadImages(validImages, identifier)
                                    .flatMapMany(Flux::fromIterable)
                                    .flatMap(url -> productImagePersistencePort.save(ProductImage.builder()
                                            .productId(savedProduct.getId())
                                            .imageUrl(url)
                                            .createdAt(LocalDateTime.now())
                                            .build()))
                                    .collectList()
                                    .map(savedImages -> {
                                        savedProduct.setImages(savedImages);
                                        return savedProduct;
                                    });
                        });
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
                                .map(ProductCategory::getId)
                                : Mono.just(existing.getCategoryId());

                        return resolvedCategoryId.flatMap(categoryId -> {
                            // 1. Update the product fields
                            Mono<Product> updatedProductMono = productPersistencePort.update(name, Product.builder()
                                    .categoryId(categoryId)
                                    .name(request.getName() != null ? request.getName() : existing.getName())
                                    .description(request.getDescription() != null ? request.getDescription() : existing.getDescription())
                                    .quantity(request.getQuantity() != null ? request.getQuantity() : existing.getQuantity())
                                    .price(request.getPrice() != null ? request.getPrice() : existing.getPrice())
                                    .sku(request.getSku() != null ? request.getSku() : existing.getSku())
                                    .status(request.getStatus() != null ? request.getStatus() : existing.getStatus())
                                    .updatedAt(LocalDateTime.now())
                                    .build());

                            return updatedProductMono.flatMap(updatedProduct -> {
                                List<MultipartFile> validImages = newImages != null
                                        ? newImages.stream().filter(f -> f != null && !f.isEmpty()).toList()
                                        : List.of();

                                if (validImages.isEmpty()) {
                                    // No new images — just enrich with existing ones from DB
                                    return enrichWithImages(updatedProduct);
                                }

                                // 2. Upload new images and save each to product_image table
                                String identifier = (request.getSku() != null && !request.getSku().isBlank())
                                        ? request.getSku()
                                        : (existing.getSku() != null && !existing.getSku().isBlank())
                                          ? existing.getSku() : existing.getName();

                                return storageService.uploadImages(validImages, identifier)
                                        .flatMapMany(Flux::fromIterable)
                                        .flatMap(url -> productImagePersistencePort.save(ProductImage.builder()
                                                .productId(updatedProduct.getId())
                                                .imageUrl(url)
                                                .createdAt(LocalDateTime.now())
                                                .build()))
                                        .collectList()
                                        .flatMap(newlySaved -> enrichWithImages(updatedProduct));
                            });
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
                .flatMap(p -> productPersistencePort.deleteByName(name)
                        .then(cleanUpContentImages(p.getId())));
        // product_image rows are deleted automatically by ON DELETE CASCADE
    }

    // Best-effort: if the "content" profile is active, tell CMS to drop this
    // product's images too. If it's not active, or CMS is unreachable, we
    // still want the IMS-side delete to have succeeded — never fail the
    // product delete because of a CMS hiccup.
    private Mono<Void> cleanUpContentImages(UUID productId) {
        ContentServiceClientPort client = contentServiceClientPort.getIfAvailable();
        if (client == null)
            return Mono.empty();

        return client.deleteAllByProductId(productId)
                .onErrorResume(e -> Mono.empty());
    }

    //-------------------- Private helpers ------------------------------------------------------

    private Mono<Product> enrichWithImages(Product product) {
        return productImagePersistencePort.findAllByProductId(product.getId())
                .collectList()
                .map(images -> {
                    product.setImages(images);
                    return product;
                });
    }
}