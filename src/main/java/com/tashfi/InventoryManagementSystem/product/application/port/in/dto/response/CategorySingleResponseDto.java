package com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.product.domain.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySingleResponseDto {
    private String message;
    private ProductCategory categoryData;
}
