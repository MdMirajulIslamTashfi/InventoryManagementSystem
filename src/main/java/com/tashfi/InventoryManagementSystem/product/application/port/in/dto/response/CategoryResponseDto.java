package com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.product.domain.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDto {
    private String message;
    private Integer totalRecords;
    private List<ProductCategory> categoryData;
}
