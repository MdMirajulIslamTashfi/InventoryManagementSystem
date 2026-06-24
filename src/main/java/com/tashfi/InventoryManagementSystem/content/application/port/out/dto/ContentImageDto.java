package com.tashfi.InventoryManagementSystem.content.application.port.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

// Mirrors com.tashfi.contentmanagementsystem.image.domain.ContentImage
// This is what CMS returns over HTTP — kept as a plain DTO here since
// IMS does not own this data, it only displays/references it.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentImageDto {
    private UUID id;
    private UUID productId;
    private String productSku;
    private String productName;
    private String imageUrl;
    private String imageName;
    private String thumbnailUrl;
    private String displayUrl;
    private Boolean isThumbnail;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}