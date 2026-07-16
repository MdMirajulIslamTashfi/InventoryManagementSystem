package com.tashfi.InventoryManagementSystem.product.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.product.application.port.in.ProductUseCase;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        log.info("Received request to fetch all products");
        return productUseCase.findAllProducts()
                .flatMap(res -> {
                    log.info("Successfully returning all products");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch all products: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        String name = request.pathVariable("name");
        log.info("Received request to search products by name: {}", name);
        return productUseCase.searchProductsByName(name)
                .flatMap(res -> {
                    log.info("Successfully returning search results for name: {}", name);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to search products by name [{}]: {}", name, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> getProductById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to fetch product by id: {}", id);
        return productUseCase.findProductById(id)
                .flatMap(res -> {
                    log.info("Successfully returning product with id: {}", id);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch product by id [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    // Now a plain JSON body — image handling moved entirely to the productimage module
    public Mono<ServerResponse> createProduct(ServerRequest request) {
        log.info("Received request to create a new product");
        return request.bodyToMono(ProductRequestDto.class)
                .flatMap(productUseCase::createProduct)
                .flatMap(res -> {
                    log.info("Product created successfully");
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to create product: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    // Now a plain JSON body — image handling moved entirely to the productimage module
    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to update product with id: {}", id);
        return request.bodyToMono(ProductUpdateRequestDto.class)
                .flatMap(dto -> productUseCase.updateProduct(id, dto))
                .flatMap(res -> {
                    log.info("Product updated successfully for id: {}", id);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(res);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to update product [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to delete product with id: {}", id);
        return productUseCase.deleteProduct(id)
                .then(Mono.defer(() -> {
                    log.info("Successfully deleted product with id: {}", id);
                    return ServerResponse.noContent().build();
                }))
                .onErrorResume(ex -> {
                    log.error("Failed to delete product [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }
}