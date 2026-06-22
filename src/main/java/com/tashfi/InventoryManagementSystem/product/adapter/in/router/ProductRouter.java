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
                        .GET(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL), productHandler::getAllProducts)
                        .GET(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.PRODUCT_SEARCH_URL).concat(RouterName.NAME), productHandler::searchProducts)
                        .POST(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.PRODUCT_ADD_URL), productHandler::createProduct)
                        .PUT(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.PRODUCT_UPDATE_URL).concat(RouterName.NAME), productHandler::updateProduct)
                        .DELETE(RouterName.BASE_URL.concat(RouterName.PRODUCT_BASE_URL).concat(RouterName.PRODUCT_DELETE_URL).concat(RouterName.NAME), productHandler::deleteProduct)
                ).build();
    }
}