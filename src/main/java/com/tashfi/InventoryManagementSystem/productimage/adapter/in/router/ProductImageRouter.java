package com.tashfi.InventoryManagementSystem.productimage.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.productimage.adapter.in.handler.ProductImageHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductImageRouter {

    @Bean
    public RouterFunction<ServerResponse> productImageRoutes(ProductImageHandler productImageHandler) {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.NAME).concat(RouterName.PRODUCT_IMAGE_URL).concat(RouterName.ID), productImageHandler::getImage)
                        .PUT(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.NAME).concat(RouterName.PRODUCT_IMAGE_URL).concat(RouterName.ID), productImageHandler::updateImage)
                        .DELETE(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.NAME).concat(RouterName.PRODUCT_IMAGE_URL).concat(RouterName.ID), productImageHandler::deleteImage)
                ).build();
    }
}