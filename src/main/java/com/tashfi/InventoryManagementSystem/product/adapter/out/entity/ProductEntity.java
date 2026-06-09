package com.tashfi.InventoryManagementSystem.product.adapter.out.entity;

import com.tashfi.InventoryManagementSystem.core.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product")
public class ProductEntity {

    @Id
    private UUID id;

    @Column("category_id")
    private UUID categoryId;

    @Transient
    private String categoryName;

    private String name;
    private String description;
    private Integer quantity;
    private BigDecimal price;
    private String sku;
    private ProductStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
