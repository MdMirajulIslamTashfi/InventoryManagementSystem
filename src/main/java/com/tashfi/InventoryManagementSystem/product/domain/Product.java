package com.tashfi.InventoryManagementSystem.product.domain;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private String sku; // Stock Keeping Unit (optional)
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
