package com.tashfi.InventoryManagementSystem.productimage.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private UUID id;
    private UUID productId;
    private String imageUrl;
    private LocalDateTime createdAt;
}
