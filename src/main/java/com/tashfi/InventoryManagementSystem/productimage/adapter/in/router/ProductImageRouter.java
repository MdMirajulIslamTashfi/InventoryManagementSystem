package com.tashfi.InventoryManagementSystem.productimage.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.productimage.adapter.in.handler.ProductImageHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@RequiredArgsConstructor
public class ProductImageRouter {

    private final ProductImageHandler handler;

    private static final String base = RouterName.BASE_URL + RouterName.PRODUCT_BASE_URL
            + RouterName.PRODUCT_ID_URL + RouterName.PRODUCT_IMAGE_URL;
    private static final String withImageId = base + RouterName.IMAGE_ID_URL;

    @Bean
    public RouterFunction<ServerResponse> productImageRoutes() {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .POST(base, handler::createImages)
                        .GET(base, handler::findAllByProductId)
                        .GET(withImageId, handler::findById)
                        .PUT(withImageId, handler::updateImage)
                        .DELETE(withImageId, handler::deleteImage)
                        .DELETE(base, handler::deleteAllByProductId)
                ).build();
    }
}