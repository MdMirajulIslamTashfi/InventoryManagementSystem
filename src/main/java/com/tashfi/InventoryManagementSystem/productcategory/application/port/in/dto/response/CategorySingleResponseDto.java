package com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.productcategory.domain.ProductCategory;
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
