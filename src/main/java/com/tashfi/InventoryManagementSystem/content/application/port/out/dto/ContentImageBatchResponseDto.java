package com.tashfi.InventoryManagementSystem.content.application.port.out.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Mirrors CMS's ContentImageResponseDto (the shape returned by
// POST/GET /api/content/products/{productId}/images)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentImageBatchResponseDto {
    private String message;
    private int totalRecords;
    private List<ContentImageDto> imageData;
}