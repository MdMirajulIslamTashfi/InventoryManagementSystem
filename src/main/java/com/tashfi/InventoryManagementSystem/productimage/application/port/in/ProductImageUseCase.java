package com.tashfi.InventoryManagementSystem.productimage.application.port.in;

import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.request.ProductImageRequestDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageResponseDto;
import com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.response.ProductImageSingleResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface ProductImageUseCase {
    Mono<ProductImageResponseDto> createImages(ProductImageRequestDto request, List<FilePart> files);
    Mono<ProductImageResponseDto> findAllByProductId(UUID productId);
    Mono<ProductImageSingleResponseDto> findById(UUID productId, UUID imageId);
    Mono<ProductImageSingleResponseDto> updateImage(UUID productId, UUID imageId, FilePart newFile, Boolean isThumbnail, String updatedBy);
    Mono<Void> deleteImage(UUID productId, UUID imageId);
    Mono<Void> deleteAllByProductId(UUID productId);
}
