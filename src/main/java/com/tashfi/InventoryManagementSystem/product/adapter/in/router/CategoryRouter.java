package com.tashfi.InventoryManagementSystem.product.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.product.adapter.in.handler.CategoryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class CategoryRouter {

    @Bean
    public RouterFunction<ServerResponse> categoryRoutes(CategoryHandler categoryHandler) {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(RouterName.CATEGORY_BASE_URL, categoryHandler::getAllCategories)
                        .GET(RouterName.CATEGORY_SEARCH_BY_NAME_URL, categoryHandler::searchCategoryByName)
                        .POST(RouterName.CATEGORY_ADD_URL, categoryHandler::createCategory)
                        .PUT(RouterName.CATEGORY_UPDATE_URL, categoryHandler::updateCategory)
                        .DELETE(RouterName.CATEGORY_DELETE_URL, categoryHandler::deleteCategory)
                ).build();
    }
}