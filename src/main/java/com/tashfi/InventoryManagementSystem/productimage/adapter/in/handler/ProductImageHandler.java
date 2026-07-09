package com.tashfi.InventoryManagementSystem.productimage.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.ProductImageUseCase;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.request.ProductImageRequestDto;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ProductImageHandler {

    private final ProductImageUseCase productImageUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    // ---------- POST /api/products/{productId}/images -----------------
    public Mono<ServerResponse> createImages(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");

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

                    if (productName == null || productName.isBlank())
                        return Mono.error(new ValidationException("Product name is required"));

                    ProductImageRequestDto dto = ProductImageRequestDto.builder()
                            .productId(productId)
                            .productName(productName)
                            .productSku(productSku)
                            .createdBy(createdBy)
                            .isThumbnail(thumbStr != null ? Boolean.parseBoolean(thumbStr) : true)
                            .build();
                    return productImageUseCase.createImages(dto, files);
                })
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result))
                .onErrorResume(this::handleError);
    }

    // ------------------GET /api/products/{productId}/images --------------------------
    public Mono<ServerResponse> findAllByProductId(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        return productImageUseCase.findAllByProductId(productId)
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result))
                .onErrorResume(this::handleError);
    }

    // ---------------- GET /api/products/{productId}/images/{imageId} — redirect to the actual file ------------
    public Mono<ServerResponse> findById(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");
        return productImageUseCase.findById(productId, imageId)
                .flatMap(result -> {
                    String url = result.getImageData().getThumbnailUrl();
                    if (url == null || url.isBlank())
                        url = result.getImageData().getImageUrl();
                    if (url == null || url.isBlank())
                        return Mono.error(new ValidationException("This image record has no stored file"));
                    return ServerResponse.temporaryRedirect(URI.create(url)).build();
                })
                .onErrorResume(this::handleError);
    }

    // ------------------- PUT /api/products/{productId}/images/{imageId} ------------------------
    public Mono<ServerResponse> updateImage(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");

        return request.multipartData()
                .flatMap(multipart -> {
                    Part filePart = multipart.getFirst("file");
                    if (!(filePart instanceof FilePart fp))
                        return Mono.error(new ValidationException("A file part named 'file' is required"));

                    String thumbStr = firstFormValue(multipart.getFirst("isThumbnail"));
                    String updatedBy = firstFormValue(multipart.getFirst("updatedBy"));
                    Boolean isThumbnail = thumbStr != null ? Boolean.parseBoolean(thumbStr) : null;

                    return productImageUseCase.updateImage(productId, imageId, fp, isThumbnail, updatedBy);
                })
                .flatMap(result -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(result))
                .onErrorResume(this::handleError);
    }

    // ----------------------- DELETE /api/products/{productId}/images/{imageId} --------------------------
    public Mono<ServerResponse> deleteImage(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        UUID imageId = parseUuid(request, "imageId");
        return productImageUseCase.deleteImage(productId, imageId)
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::handleError);
    }

    //---------------------- DELETE /api/products/{productId}/images --------------------------------------
    public Mono<ServerResponse> deleteAllByProductId(ServerRequest request) {
        UUID productId = parseUuid(request, "productId");
        return productImageUseCase.deleteAllByProductId(productId)
                .then(ServerResponse.noContent().build())
                .onErrorResume(this::handleError);
    }

    // ---------------------------------------- Helpers -------------------------------------------------------

    private Mono<ServerResponse> handleError(Throwable ex) {
        return exceptionHandler.handle(ex)
                .flatMap(err -> ServerResponse.status(err.getStatus())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(err));
    }

    private UUID parseUuid(ServerRequest request, String variable) {
        try {
            return UUID.fromString(request.pathVariable(variable));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid UUID for path variable '" + variable + "'");
        }
    }

    private String firstFormValue(Part part) {
        if (part instanceof FormFieldPart ffp)
            return ffp.value();
        return null;
    }
}
