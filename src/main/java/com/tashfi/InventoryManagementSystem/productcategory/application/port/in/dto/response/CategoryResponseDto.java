package com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.productcategory.domain.ProductCategory;
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
