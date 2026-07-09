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
@Table(name = "product_image")
public class ProductImageEntity {
    @Id
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("product_sku")
    private String productSku;

    @Column("product_name")
    private String productName;

    @Column("image_url")
    private String imageUrl;

    @Column("image_name")
    private String imageName;

    @Column("is_thumbnail")
    private Boolean isThumbnail;

    @Column("thumbnail_url")
    private String thumbnailUrl;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("created_by")
    private String createdBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("updated_by")
    private String updatedBy;
}
