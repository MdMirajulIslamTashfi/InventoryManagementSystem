package com.tashfi.InventoryManagementSystem.productimage.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.ProductImageUseCase;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.request.ProductImageRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductImageHandler {

    private final ProductImageUseCase productImageUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    // ---------- POST /api/products/{productId}/images -----------------
    public Mono<ServerResponse> createImages(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        log.info("Received request to create image(s) for product id: {}", productId);

        return request.multipartData()
                .flatMap(multipart -> {
                    List<FilePart> files = multipart.get("files") == null ? List.of()
                            : multipart.get("files").stream()
                            .filter(p -> p instanceof FilePart)
                            .map(p -> (FilePart) p)
                            .toList();
                    String productName = firstFormValue(multipart.getFirst("productName"));
                    String productSku = firstFormValue(multipart.getFirst("productSku"));
                    String createdBy = firstFormValue(multipart.getFirst("createdBy"));
                    String thumbStr = firstFormValue(multipart.getFirst("isThumbnail"));

                    if (productName == null || productName.isBlank()) {
                        log.warn("Create images failed - product name missing for product id: {}", productId);
                        return Mono.error(new ValidationException("Product name is required"));
                    }

                    log.debug("Parsed create-images request for product id: {} with {} file(s)",
                            productId, files.size());

                    ProductImageRequestDto dto = ProductImageRequestDto.builder()
                            .productId(productId)
                            .productName(productName)
                            .productSku(productSku)
                            .createdBy(createdBy)
                            .isThumbnail(thumbStr != null ? Boolean.parseBoolean(thumbStr) : true)
                            .build();
                    return productImageUseCase.createImages(dto, files);
                })
                .flatMap(result -> {
                    log.info("Image(s) created successfully for product id: {}", productId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(result);
                })
                .onErrorResume(ex -> handleError(ex, "createImages", productId));
    }

    // ------------------GET /api/products/{productId}/images --------------------------
    public Mono<ServerResponse> findAllByProductId(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        log.info("Received request to fetch all images for product id: {}", productId);
        return productImageUseCase.findAllByProductId(productId)
                .flatMap(result -> {
                    log.info("Successfully returning images for product id: {}", productId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(result);
                })
                .onErrorResume(ex -> handleError(ex, "findAllByProductId", productId));
    }

    // ---------------- GET /api/products/{productId}/images/{imageId} — redirect to the actual file ------------
    public Mono<ServerResponse> findById(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");
        log.info("Received request to fetch image [{}] for product id: {}", imageId, productId);
        return productImageUseCase.findById(productId, imageId)
                .flatMap(result -> {
                    String url = result.getImageData().getThumbnailUrl();
                    if (url == null || url.isBlank())
                        url = result.getImageData().getImageUrl();
                    if (url == null || url.isBlank()) {
                        log.warn("Image [{}] for product id [{}] has no stored file", imageId, productId);
                        return Mono.error(new ValidationException("This image record has no stored file"));
                    }
                    log.info("Redirecting to stored file for image [{}], product id: {}", imageId, productId);
                    return ServerResponse.temporaryRedirect(URI.create(url)).build();
                })
                .onErrorResume(ex -> handleError(ex, "findById", productId, imageId));
    }

    // ------------------- PUT /api/products/{productId}/images/{imageId} ------------------------
    public Mono<ServerResponse> updateImage(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");
        log.info("Received request to update image [{}] for product id: {}", imageId, productId);

        return request.multipartData()
                .flatMap(multipart -> {
                    Part filePart = multipart.getFirst("file");
                    if (!(filePart instanceof FilePart fp)) {
                        log.warn("Update image failed - no file part named 'file' for image [{}]", imageId);
                        return Mono.error(new ValidationException("A file part named 'file' is required"));
                    }

                    String thumbStr = firstFormValue(multipart.getFirst("isThumbnail"));
                    String updatedBy = firstFormValue(multipart.getFirst("updatedBy"));
                    Boolean isThumbnail = thumbStr != null ? Boolean.parseBoolean(thumbStr) : null;

                    return productImageUseCase.updateImage(productId, imageId, fp, isThumbnail, updatedBy);
                })
                .flatMap(result -> {
                    log.info("Image [{}] updated successfully for product id: {}", imageId, productId);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(result);
                })
                .onErrorResume(ex -> handleError(ex, "updateImage", productId, imageId));
    }

    // ----------------------- DELETE /api/products/{productId}/images/{imageId} --------------------------
    public Mono<ServerResponse> deleteImage(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");
        log.info("Received request to delete image [{}] for product id: {}", imageId, productId);
        return productImageUseCase.deleteImage(productId, imageId)
                .then(Mono.defer(() -> {
                    log.info("Image [{}] deleted successfully for product id: {}", imageId, productId);
                    return ServerResponse.noContent().build();
                }))
                .onErrorResume(ex -> handleError(ex, "deleteImage", productId, imageId));
    }

    //---------------------- DELETE /api/products/{productId}/images --------------------------------------
    public Mono<ServerResponse> deleteAllByProductId(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        log.info("Received request to delete all images for product id: {}", productId);
        return productImageUseCase.deleteAllByProductId(productId)
                .then(Mono.defer(() -> {
                    log.info("All images deleted successfully for product id: {}", productId);
                    return ServerResponse.noContent().build();
                }))
                .onErrorResume(ex -> handleError(ex, "deleteAllByProductId", productId));
    }

    // ---------------------------------------- Helpers -------------------------------------------------------

    private Mono<ServerResponse> handleError(Throwable ex, String operation, UUID productId) {
        log.error("Failed to execute {} for product id [{}]: {}", operation, productId, ex.getMessage(), ex);
        return exceptionHandler.handle(ex)
                .flatMap(err -> ServerResponse.status(err.getStatus())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(err));
    }

    private Mono<ServerResponse> handleError(Throwable ex, String operation, UUID productId, UUID imageId) {
        log.error("Failed to execute {} for image [{}], product id [{}]: {}",
                operation, imageId, productId, ex.getMessage(), ex);
        return exceptionHandler.handle(ex)
                .flatMap(err -> ServerResponse.status(err.getStatus())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(err));
    }

    private UUID parseUuid(ServerRequest request, String variable) {
        try {
            return UUID.fromString(request.pathVariable(variable));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID for path variable '{}': {}", variable, request.pathVariable(variable));
            throw new ValidationException("Invalid UUID for path variable '" + variable + "'");
        }
    }

    private String firstFormValue(Part part) {
        if (part instanceof FormFieldPart ffp)
            return ffp.value();
        return null;
    }
}