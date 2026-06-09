package com.tashfi.InventoryManagementSystem.product.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.product.application.port.in.ProductUseCase;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductRequestDto;
import com.tashfi.InventoryManagementSystem.product.application.port.in.dto.request.ProductUpdateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;

    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        return productUseCase.findAllProducts()
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> searchProducts(ServerRequest request) {
        String name = request.pathVariable("name");
        return productUseCase.searchProductsByName(name)
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return request.bodyToMono(ProductRequestDto.class)
                .flatMap(productUseCase::createProduct)
                .flatMap(res -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        String name = request.pathVariable("name");
        return request.bodyToMono(ProductUpdateRequestDto.class)
                .flatMap(dto -> productUseCase.updateProduct(name, dto))
                .flatMap(res -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(res));
    }

    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        String name = request.pathVariable("name");
        return productUseCase.deleteProduct(name)
                .then(ServerResponse.noContent().build());
    }
}