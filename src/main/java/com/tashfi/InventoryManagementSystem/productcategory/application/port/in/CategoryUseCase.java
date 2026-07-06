package com.tashfi.InventoryManagementSystem.productcategory.application.port.in;

import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.request.CategoryRequestDto;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response.CategoryResponseDto;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.response.CategorySingleResponseDto;
import reactor.core.publisher.Mono;

public interface CategoryUseCase {
    Mono<CategoryResponseDto> findAllCategories();
    Mono<CategorySingleResponseDto> searchCategoryByName(String name);
    Mono<CategorySingleResponseDto> createCategory(CategoryRequestDto request);
    Mono<CategorySingleResponseDto> updateCategory(String name, CategoryRequestDto request);
    Mono<Void> deleteCategory(String name);
}