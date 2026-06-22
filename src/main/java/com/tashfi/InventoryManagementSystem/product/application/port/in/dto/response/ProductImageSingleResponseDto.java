package com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.product.domain.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageSingleResponseDto {
    private String message;
    private ProductImage imageData;
}
