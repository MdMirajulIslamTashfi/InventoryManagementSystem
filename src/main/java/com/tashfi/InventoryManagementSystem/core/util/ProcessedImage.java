package com.tashfi.InventoryManagementSystem.core.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedImage {
    private byte[] originalBytes;
    private String originalFileName;
    private byte[] thumbnailBytes;    // null if generateThumbnail = false
    private String thumbnailFileName; // null if generateThumbnail = false
    private String contentType;
}
