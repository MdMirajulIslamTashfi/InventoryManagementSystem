package com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.product.domain.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductSingleResponseDto {
    private String message;
    private Product productData;
}
