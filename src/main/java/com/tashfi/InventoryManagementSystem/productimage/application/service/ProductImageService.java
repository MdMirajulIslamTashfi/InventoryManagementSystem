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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductImageService implements ProductImageUseCase {

    private static final int MAX_IMAGES_PER_UPLOAD = 5;

    private final ProductImagePersistencePort productImagePersistencePort;
    private final StorageService storageService;

    //-------------------- Create image-------------------------------------
    @Override
    public Mono<ProductImageResponseDto> createImages(ProductImageRequestDto request, List<FilePart> files) {
        log.info("Creating image(s) for product id: {}, name: {}", request.getProductId(), request.getProductName());

        if (request.getProductId() == null) {
            log.warn("Create images failed - product id is missing");
            return Mono.error(new ValidationException("Product is required"));
        }
        if (request.getProductName() == null || request.getProductName().isEmpty()) {
            log.warn("Create images failed - product name is missing for product id: {}", request.getProductId());
            return Mono.error(new ValidationException("Product name is required"));
        }

        // files can be null / empty - that is allowed
        List<FilePart> safeFiles = files == null ? List.of() : files;

        if (safeFiles.size() > MAX_IMAGES_PER_UPLOAD) {
            log.warn("Create images failed - {} files exceed max of {} for product id: {}",
                    safeFiles.size(), MAX_IMAGES_PER_UPLOAD, request.getProductId());
            return Mono.error(new ValidationException("Maximum " + MAX_IMAGES_PER_UPLOAD + " images per upload"));
        }

        if (safeFiles.isEmpty()) {
            log.debug("No files provided for product id: {} - creating metadata-only record", request.getProductId());
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
                    .map(saved -> {
                        log.info("Metadata-only image record created for product id: {}", request.getProductId());
                        return ProductImageResponseDto.builder()
                                .message("No images provided; record created with no image data")
                                .totalRecords(1)
                                .imageData(List.of(saved))
                                .build();
                    })
                    .doOnError(ex -> log.error("Error creating metadata-only record for product id [{}]: {}",
                            request.getProductId(), ex.getMessage(), ex));
        }

        String identifier = request.getProductId().toString();
        log.debug("Uploading {} file(s) for product id: {}", safeFiles.size(), request.getProductId());

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
                            .flatMap(productImagePersistencePort::save)
                            .doOnSuccess(saved -> log.debug("Uploaded and saved image '{}' for product id: {}",
                                    fp.filename(), request.getProductId()));
                })
                .collectList()
                .map(saved -> {
                    log.info("{} image(s) uploaded successfully for product id: {}", saved.size(), request.getProductId());
                    return ProductImageResponseDto.builder()
                            .message(saved.size() + " image(s) uploaded successfully")
                            .totalRecords(saved.size())
                            .imageData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while creating images for product id [{}]: {}",
                        request.getProductId(), ex.getMessage(), ex));
    }

    // --------------------------find all image of a product-------------------------------
    @Override
    public Mono<ProductImageResponseDto> findAllByProductId(UUID productId) {
        log.info("Fetching all images for product id: {}", productId);
        return productImagePersistencePort.findAllByProductId(productId)
                .collectList()
                .map(list -> {
                    log.info("Fetched {} image(s) for product id: {}", list.size(), productId);
                    return ProductImageResponseDto.builder()
                            .message("Images retrieved successfully")
                            .totalRecords(list.size())
                            .imageData(list)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching images for product id [{}]: {}",
                        productId, ex.getMessage(), ex));
    }

    // ------------------------------find specific image-------------------------
    @Override
    public Mono<ProductImageSingleResponseDto> findById(UUID productId, UUID imageId) {
        log.info("Fetching image [{}] for product id: {}", imageId, productId);
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Image not found - imageId: {}, productId: {}", imageId, productId);
                    return Mono.error(new ProductImageNotFoundException(
                            "Image not found with id: " + imageId + " for product: " + productId));
                }))
                .map(image -> {
                    log.info("Image fetched successfully - imageId: {}, productId: {}", imageId, productId);
                    return ProductImageSingleResponseDto.builder()
                            .message("Image retrieved successfully")
                            .imageData(image)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching image [{}] for product id [{}]: {}",
                        imageId, productId, ex.getMessage(), ex));
    }

    //------------------------------------ update image-----------------------------------
    @Override
    public Mono<ProductImageSingleResponseDto> updateImage(UUID productId, UUID imageId, FilePart newFile, Boolean isThumbnail, String updatedBy) {
        log.info("Updating image [{}] for product id: {}", imageId, productId);
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Update failed - image not found. imageId: {}, productId: {}", imageId, productId);
                    return Mono.error(new ProductImageNotFoundException(
                            "Image not found with id: " + imageId + " for product: " + productId));
                }))
                .flatMap(existing -> {
                    boolean thumb = isThumbnail != null ? isThumbnail
                            : (existing.getIsThumbnail() != null && existing.getIsThumbnail());
                    log.debug("Uploading replacement file '{}' for image [{}]", newFile.filename(), imageId);
                    return storageService.uploadImage(newFile, productId.toString(), thumb)
                            .flatMap(uploaded -> {
                                // Delete old files from storage (best-effort, don't fail on error)
                                Mono<Void> deleteOld = Mono.when(
                                                storageService.deleteImage(existing.getImageUrl()),
                                                storageService.deleteImage(existing.getThumbnailUrl()))
                                        .doOnError(e -> log.warn("Failed to delete old file(s) for image [{}] - continuing: {}",
                                                imageId, e.getMessage()))
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
                .map(saved -> {
                    log.info("Image updated successfully - imageId: {}, productId: {}", imageId, productId);
                    return ProductImageSingleResponseDto.builder()
                            .message("Image updated successfully")
                            .imageData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while updating image [{}] for product id [{}]: {}",
                        imageId, productId, ex.getMessage(), ex));
    }

    // ---------------------- delete -------------------------
    @Override
    public Mono<Void> deleteImage(UUID productId, UUID imageId) {
        log.info("Deleting image [{}] for product id: {}", imageId, productId);
        return productImagePersistencePort.findByIdAndProductId(imageId, productId)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Delete failed - image not found. imageId: {}, productId: {}", imageId, productId);
                    return Mono.error(new ProductImageNotFoundException(
                            "Image not found with id: " + imageId + " for product: " + productId));
                }))
                .flatMap(existing -> {
                    Mono<Void> deleteFiles = Mono.when(
                                    storageService.deleteImage(existing.getImageUrl()),
                                    storageService.deleteImage(existing.getThumbnailUrl())
                            ).doOnError(e -> log.warn("Failed to delete file(s) from storage for image [{}] - continuing: {}",
                                    imageId, e.getMessage()))
                            .onErrorResume(e -> Mono.empty());

                    return deleteFiles.then(
                            productImagePersistencePort.deleteByIdAndProductId(imageId, productId)
                                    .doOnSuccess(v -> log.info("Image deleted successfully - imageId: {}, productId: {}",
                                            imageId, productId)));
                })
                .doOnError(ex -> log.error("Error while deleting image [{}] for product id [{}]: {}",
                        imageId, productId, ex.getMessage(), ex));
    }

    @Override
    public Mono<Void> deleteAllByProductId(UUID productId) {
        log.info("Deleting all images for product id: {}", productId);
        return productImagePersistencePort.findAllByProductId(productId)
                .flatMap(image -> Mono.when(
                                storageService.deleteImage(image.getImageUrl()),
                                storageService.deleteImage(image.getThumbnailUrl())
                        ).doOnError(e -> log.warn("Failed to delete file(s) from storage for image [{}] - continuing: {}",
                                image.getId(), e.getMessage()))
                        .onErrorResume(e -> Mono.empty()))
                .then(productImagePersistencePort.deleteAllByProductId(productId))
                .doOnSuccess(v -> log.info("All images deleted successfully for product id: {}", productId))
                .doOnError(ex -> log.error("Error while deleting all images for product id [{}]: {}",
                        productId, ex.getMessage(), ex));
    }
}