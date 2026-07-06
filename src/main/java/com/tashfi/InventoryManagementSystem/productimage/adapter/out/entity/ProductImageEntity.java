package com.tashfi.InventoryManagementSystem.productimage.adapter.out.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name= "product_image")
public class ProductImageEntity {
    @Id
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("image_url")
    private String imageUrl;

    @Column("created_at")
    private LocalDateTime createdAt;
}
