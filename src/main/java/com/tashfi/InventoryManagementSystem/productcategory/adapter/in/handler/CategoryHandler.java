package com.tashfi.InventoryManagementSystem.productcategory.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.CategoryUseCase;
import com.tashfi.InventoryManagementSystem.productcategory.application.port.in.dto.request.CategoryRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoryHandler {

    private final CategoryUseCase categoryUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    public Mono<ServerResponse> getAllCategories(ServerRequest request) {
        log.info("Received request to fetch all categories");
        return categoryUseCase.findAllCategories()
                .flatMap(res -> {
                    log.info("Successfully returning all categories");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch all categories: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> searchCategoryByName(ServerRequest request) {
        String name = request.pathVariable("name");
        log.info("Received request to search category by name: {}", name);
        return categoryUseCase.searchCategoryByName(name)
                .flatMap(res -> {
                    log.info("Successfully returning category for name: {}", name);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to search category by name [{}]: {}", name, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> createCategory(ServerRequest request) {
        log.info("Received request to create a new category");
        return request.bodyToMono(CategoryRequestDto.class)
                .flatMap(categoryUseCase::createCategory)
                .flatMap(res -> {
                    log.info("Category created successfully");
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to create category: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> updateCategory(ServerRequest request) {
        String name = request.pathVariable("name");
        log.info("Received request to update category with name: {}", name);
        return request.bodyToMono(CategoryRequestDto.class)
                .flatMap(dto -> categoryUseCase.updateCategory(name, dto))
                .flatMap(res -> {
                    log.info("Category updated successfully for name: {}", name);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to update category [{}]: {}", name, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> deleteCategory(ServerRequest request) {
        String name = request.pathVariable("name");
        log.info("Received request to delete category with name: {}", name);
        return categoryUseCase.deleteCategory(name)
                .then(Mono.defer(() -> {
                    log.info("Successfully deleted category with name: {}", name);
                    return ServerResponse.noContent().build();
                }))
                .onErrorResume(ex -> {
                    log.error("Failed to delete category [{}]: {}", name, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }
}