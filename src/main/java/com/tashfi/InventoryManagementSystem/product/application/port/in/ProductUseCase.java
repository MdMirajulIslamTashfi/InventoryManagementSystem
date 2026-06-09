package com.tashfi.InventoryManagementSystem.product.application.port.in;

import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductSingleResponseDto;
import reactor.core.publisher.Mono;

public interface ProductUseCase {
    Mono<ProductResponseDto> findAllProducts();
    Mono<ProductResponseDto> searchProductsByName(String name);
    Mono<ProductSingleResponseDto> createProduct(ProductRequestDto request);
    Mono<ProductSingleResponseDto> updateProduct(String name, ProductUpdateRequestDto request);
    Mono<Void> deleteProduct(String name);
}