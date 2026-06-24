package com.tashfi.InventoryManagementSystem.content.application.port.out;

import com.tashfi.InventoryManagementSystem.content.application.port.out.dto.ContentImageBatchResponseDto;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

// Outbound port: how IMS talks to the CMS microservice for product images.
// Implemented only when the "content" profile is active (see ContentServiceClientAdapter).
public interface ContentServiceClientPort {

    Mono<ContentImageBatchResponseDto> uploadImages(UUID productId,
                                                    String productName,
                                                    String productSku,
                                                    String createdBy,
                                                    Boolean isThumbnail,
                                                    List<MultipartFile> files);

    Mono<ContentImageBatchResponseDto> findAllByProductId(UUID productId);

    Mono<Void> deleteImage(UUID productId, UUID imageId);

    Mono<Void> deleteAllByProductId(UUID productId);
}