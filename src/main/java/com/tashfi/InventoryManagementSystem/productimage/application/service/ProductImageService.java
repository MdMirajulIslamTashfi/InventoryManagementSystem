package com.tashfi.InventoryManagementSystem.productimage.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.ProductImageNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.storage.StorageService;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.ProductImageUseCase;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.request.ProductImageRequestDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageResponseDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageSingleResponseDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.out.ProductImagePersistencePort;
import com.tashfi.InventoryManagementSystem.productimage.domain.ProductImage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductImageService implements ProductImageUseCase {

    private static final int MAX_IMAGES_PER_UPLOAD = 5;

    private final ProductImagePersistencePort productImagePersistencePort;
    private final StorageService storageService;

    //-------------------- Create image-------------------------------------
    @Override
    public Mono<ProductImageResponseDto> createImages(ProductImageRequestDto request, List<FilePart> files) {
        if (request.getProductId() == null)
            return Mono.error(new ValidationException("Product is required"));
        if (request.getProductName() == null || request.getProductName().isEmpty())
            return Mono.error(new ValidationException("Product name is required"));

        // files can be null / empty - that is allowed
        List<FilePart> safeFiles = files == null ? List.of() : files;

        if (safeFiles.size() > MAX_IMAGES_PER_UPLOAD)
            return Mono.error(new ValidationException("Maximum " + MAX_IMAGES_PER_UPLOAD + " images per upload"));

        if (safeFiles.isEmpty()) {
            // No files - persist a metadata-only record with no URLs
            ProductImage noImage = ProductImage.builder()
                    .productId(request.getProductId())
                    .productSku(request.getProductSku())
                    .productName(request.getProductName())
                    .imageUrl("")
                    .imageName("")
                    .isThumbnail(false)
                    .thumbnailUrl("")
                    .createdAt(LocalDateTime.now())
                    .createdBy(request.getCreatedBy())
                    .build();
            return productImagePersistencePort.save(noImage)
                    .map(saved -> ProductImageResponseDto.builder()
                            .message("No images provided; record created with no image data")
                            .totalRecords(1)
                            .imageData(List.of(saved))
                            .build());
        }

        String identifier = request.getProductId().toString();

        return Flux.fromIterable(safeFiles)
                .index()
                .flatMap(indexed -> {
                    long idx = indexed.getT1();
                    FilePart fp = indexed.getT2();
                    boolean isThumbnail = (idx == 0) && (request.getIsThumbnail() == null || request.getIsThumbnail());

                    return storageService.uploadImage(fp, identifier, isThumbnail)
                            .map(uploaded -> ProductImage.builder()
                                    .productId(request.getProductId())
                                    .productSku(request.getProductSku())
                                    .productName(request.getProductName())
                                    .imageUrl(uploaded.getImageUrl())
                                    .imageName(uploaded.getImageName())
                                    .thumbnailUrl(uploaded.getThumbnailUrl())
                                    .isThumbnail(isThumbnail)
                                    .createdAt(LocalDateTime.now())
                                    .createdBy(request.getCreatedBy())
                                    .build())
                            .flatMap(productImagePersistencePort::save);
                })
                .collectList()
                .map(saved -> ProductImageResponseDto.builder()
                        .message(saved.size() + " image(s) uploaded successfully")
                        .totalRecords(saved.size())
                        .imageData(saved)
                        .build());
    }

    // --------------------------find all image of a product-------------------------------
    @Override
    public Mono<ProductImageResponseDto> findAllByProductId(UUID productId) {
        return productImagePersistencePort.findAllByProductId(productId)
                .collectList()
                .map(list -> ProductImageResponseDto.builder()
                        .message("Images retrieved successfully")
                        .totalRecords(list.size())
                        .imageData(list)
                        .build());
    }

    // ------------------------------find specific image-------------------------
    @Override
    public Mono<ProductImageSingleResponseDto> findById(UUID productId, UUID imageId) {
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.error(new ProductImageNotFoundException(
                        "Image not found with id: " + imageId + " for product: " + productId)))
                .map(image -> ProductImageSingleResponseDto.builder()
                        .message("Image retrieved successfully")
                        .imageData(image)
                        .build());
    }

    //------------------------------------ update image-----------------------------------
    @Override
    public Mono<ProductImageSingleResponseDto> updateImage(UUID productId, UUID imageId, FilePart newFile, Boolean isThumbnail, String updatedBy) {
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.error(new ProductImageNotFoundException(
                        "Image not found with id: " + imageId + " for product: " + productId)))
                .flatMap(existing -> {
                    boolean thumb = isThumbnail != null ? isThumbnail
                            : (existing.getIsThumbnail() != null && existing.getIsThumbnail());
                    return storageService.uploadImage(newFile, productId.toString(), thumb)
                            .flatMap(uploaded -> {
                                // Delete old files from storage (best-effort, don't fail on error)
                                Mono<Void> deleteOld = Mono.when(
                                                storageService.deleteImage(existing.getImageUrl()),
                                                storageService.deleteImage(existing.getThumbnailUrl()))
                                        .onErrorResume(e -> Mono.empty());
                                ProductImage updated = ProductImage.builder()
                                        .id(existing.getId())
                                        .productId(existing.getProductId())
                                        .productSku(existing.getProductSku())
                                        .productName(existing.getProductName())
                                        .imageUrl(uploaded.getImageUrl())
                                        .imageName(uploaded.getImageName())
                                        .thumbnailUrl(uploaded.getThumbnailUrl())
                                        .isThumbnail(thumb)
                                        .createdAt(existing.getCreatedAt())
                                        .createdBy(existing.getCreatedBy())
                                        .updatedAt(LocalDateTime.now())
                                        .updatedBy(updatedBy)
                                        .build();
                                return deleteOld.then(productImagePersistencePort.update(updated));
                            });
                })
                .map(saved -> ProductImageSingleResponseDto.builder()
                        .message("Image updated successfully")
                        .imageData(saved)
                        .build());
    }

    // ---------------------- delete -------------------------
    @Override
    public Mono<Void> deleteImage(UUID productId, UUID imageId) {
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.error(new ProductImageNotFoundException(
                        "Image not found with id: " + imageId + " for product: " + productId)))
                .flatMap(existing -> {
                    Mono<Void> deleteFiles = Mono.when(
                            storageService.deleteImage(existing.getImageUrl()),
                            storageService.deleteImage(existing.getThumbnailUrl())
                    ).onErrorResume(e -> Mono.empty());

                    return deleteFiles.then(
                            productImagePersistencePort.deleteByIdAndProductId(imageId, productId));
                });
    }

    @Override
    public Mono<Void> deleteAllByProductId(UUID productId) {
        return productImagePersistencePort.findAllByProductId(productId)
                .flatMap(image -> Mono.when(
                        storageService.deleteImage(image.getImageUrl()),
                        storageService.deleteImage(image.getThumbnailUrl())
                ).onErrorResume(e -> Mono.empty()))
                .then(productImagePersistencePort.deleteAllByProductId(productId));
    }
}
