package com.tashfi.InventoryManagementSystem.core.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadedImageUrls {
    private String imageUrl;
    private String imageName;
    private String thumbnailUrl; // null if not requested
}
