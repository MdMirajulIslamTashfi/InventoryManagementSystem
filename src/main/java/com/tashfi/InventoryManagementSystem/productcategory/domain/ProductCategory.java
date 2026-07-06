package com.tashfi.InventoryManagementSystem.productcategory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
}
