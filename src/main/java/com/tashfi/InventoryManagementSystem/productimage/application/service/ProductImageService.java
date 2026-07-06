package com.tashfi.InventoryManagementSystem.productimage.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.ProductNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.storage.StorageService;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.ProductImageUseCase;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageSingleResponseDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.out.ProductImagePersistencePort;
import com.tashfi.InventoryManagementSystem.product.application.port.out.ProductPersistencePort;
import com.tashfi.InventoryManagementSystem.product.domain.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService implements ProductImageUseCase {

    private final ProductPersistencePort productPersistencePort;
    private final ProductImagePersistencePort productImagePersistencePort;
    private final StorageService storageService;

    public ProductImageService(ProductPersistencePort productPersistencePort,
                               ProductImagePersistencePort productImagePersistencePort,
                               StorageService storageService) {
        this.productPersistencePort = productPersistencePort;
        this.productImagePersistencePort = productImagePersistencePort;
        this.storageService = storageService;
    }

    @Override
    public Mono<ProductImageSingleResponseDto> findImageById(String productName, UUID imageId) {
        return resolveProduct(productName)
                .flatMap(product -> productImagePersistencePort
                        .findByIdAndProductId(imageId, product.getId())
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(
                                "Image not found with id: " + imageId))))
                .map(image -> ProductImageSingleResponseDto.builder()
                        .message("Image fetched successfully")
                        .imageData(image)
                        .build());
    }

    @Override
    public Mono<ProductImageSingleResponseDto> updateImage(String productName, UUID imageId, MultipartFile newFile) {
        if (newFile == null || newFile.isEmpty())
            return Mono.error(new ValidationException("No image file provided"));

        return resolveProduct(productName)
                .flatMap(product -> productImagePersistencePort
                        .findByIdAndProductId(imageId, product.getId())
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(
                                "Image not found with id: " + imageId)))
                        .flatMap(existingImage -> {
                            String identifier = (product.getSku() != null && !product.getSku().isBlank())
                                    ? product.getSku() : product.getName();

                            return storageService.uploadImages(List.of(newFile), identifier)
                                    .flatMap(urls -> {
                                        String newUrl = urls.get(0);
                                        return productImagePersistencePort.update(imageId, product.getId(), newUrl)
                                                .flatMap(updated -> storageService.deleteImage(existingImage.getImageUrl())
                                                        .thenReturn(updated));
                                    });
                        }))
                .map(updated -> ProductImageSingleResponseDto.builder()
                        .message("Image updated successfully")
                        .imageData(updated)
                        .build());
    }

    @Override
    public Mono<Void> deleteImage(String productName, UUID imageId) {
        return resolveProduct(productName)
                .flatMap(product -> productImagePersistencePort
                        .findByIdAndProductId(imageId, product.getId())
                        .switchIfEmpty(Mono.error(new ProductNotFoundException(
                                "Image not found with id: " + imageId)))
                        .flatMap(existing -> productImagePersistencePort
                                .deleteByIdAndProductId(imageId, product.getId())
                                .then(storageService.deleteImage(existing.getImageUrl()))));
    }

    // --------------- Private helpers ---------------------------------------------

    // Resolve product name → full Product (need SKU for filename identifier, not just ID)
    private Mono<Product> resolveProduct(String productName) {
        return productPersistencePort.findByName(productName)
                .switchIfEmpty(Mono.error(new ProductNotFoundException(
                        "Product not found: " + productName)));
    }
}