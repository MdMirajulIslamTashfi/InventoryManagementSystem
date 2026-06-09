package com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductUpdateRequestDto {
    private String categoryName;
    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private String sku;
    private ProductStatus status;
}