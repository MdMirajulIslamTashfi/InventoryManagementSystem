package com.tashfi.InventoryManagementSystem.productimage.application.port.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequestDto {
    private UUID productId;
    private String productSku;
    private String productName;
    private Boolean isThumbnail;
    private String createdBy;
}
