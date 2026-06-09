package com.tashfi.InventoryManagementSystem.product.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.product.adapter.in.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ProductRouter {

    @Bean
    public RouterFunction<ServerResponse> productRoutes(ProductHandler productHandler) {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(RouterName.PRODUCT_BASE_URL, productHandler::getAllProducts)
                        .GET(RouterName.PRODUCT_SEARCH_BY_NAME_URL, productHandler::searchProducts)
                        .POST(RouterName.PRODUCT_ADD_URL, productHandler::createProduct)
                        .PUT(RouterName.PRODUCT_UPDATE_URL, productHandler::updateProduct)
                        .DELETE(RouterName.PRODUCT_DELETE_URL, productHandler::deleteProduct)
                ).build();
    }
}