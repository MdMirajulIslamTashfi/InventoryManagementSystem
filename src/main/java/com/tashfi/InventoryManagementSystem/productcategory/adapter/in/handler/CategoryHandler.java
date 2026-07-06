package com.tashfi.InventoryManagementSystem.productcategory.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.CategoryUseCase;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.request.CategoryRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CategoryHandler {

    private final CategoryUseCase categoryUseCase;

    public Mono<ServerResponse> getAllCategories(ServerRequest request) {
        return categoryUseCase.findAllCategories()
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> searchCategoryByName(ServerRequest request) {
        String name = request.pathVariable("name");
        return categoryUseCase.searchCategoryByName(name)
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> createCategory(ServerRequest request) {
        return request.bodyToMono(CategoryRequestDto.class)
                .flatMap(categoryUseCase::createCategory)
                .flatMap(res -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> updateCategory(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(CategoryRequestDto.class)
                .flatMap(dto -> categoryUseCase.updateCategory(name, dto))
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> deleteCategory(ServerRequest request) {
        String name = request.pathVariable("name");
        return categoryUseCase.deleteCategory(name)
                .then(ServerResponse.noContent().build());
    }
}