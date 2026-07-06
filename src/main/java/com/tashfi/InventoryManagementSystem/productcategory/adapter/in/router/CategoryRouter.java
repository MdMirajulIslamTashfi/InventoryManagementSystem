package com.tashfi.InventoryManagementSystem.productcategory.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.productcategory.adapter.in.handler.CategoryHandler;
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
                        .GET(RouterName.BASE_URL.concat(RouterName.CATEGORY_BASE_URL), categoryHandler::getAllCategories)
                        .GET(RouterName.BASE_URL.concat(RouterName.CATEGORY_BASE_URL).concat(RouterName.CATEGORY_SEARCH_URL).concat(RouterName.NAME), categoryHandler::searchCategoryByName)
                        .POST(RouterName.BASE_URL.concat(RouterName.CATEGORY_BASE_URL).concat(RouterName.CATEGORY_ADD_URL), categoryHandler::createCategory)
                        .PUT(RouterName.BASE_URL.concat(RouterName.CATEGORY_BASE_URL).concat(RouterName.CATEGORY_UPDATE_URL).concat(RouterName.NAME), categoryHandler::updateCategory)
                        .DELETE(RouterName.BASE_URL.concat(RouterName.CATEGORY_BASE_URL).concat(RouterName.CATEGORY_DELETE_URL).concat(RouterName.NAME), categoryHandler::deleteCategory)
                ).build();
    }
}