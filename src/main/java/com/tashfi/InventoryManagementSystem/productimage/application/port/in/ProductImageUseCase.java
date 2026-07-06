package com.tashfi.InventoryManagementSystem.productimage.application.port.in;

import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageResponseDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageSingleResponseDto;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductImageUseCase {

    // GET /api/products/{name}/images/{id}
    Mono<ProductImageSingleResponseDto> findImageById(String productName, UUID imageId);

    // PUT /api/products/{name}/images/{id}
    Mono<ProductImageSingleResponseDto> updateImage(String productName, UUID imageId, MultipartFile newFile);

    // DELETE /api/products/{name}/images/{id}
    Mono<Void> deleteImage(String productName, UUID imageId);
}