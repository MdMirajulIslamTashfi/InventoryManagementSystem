package com.tashfi.InventoryManagementSystem.product.application.port.in;

import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductResponseDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.response.ProductSingleResponseDto;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ProductUseCase {
    Mono<ProductResponseDto> findAllProducts();
    Mono<ProductResponseDto> searchProductsByName(String name);
    Mono<ProductSingleResponseDto> createProduct(ProductRequestDto request, List<MultipartFile> images);
    Mono<ProductSingleResponseDto> updateProduct(String name, ProductUpdateRequestDto request, List<MultipartFile> newImages);
    Mono<Void> deleteProduct(String name);
}