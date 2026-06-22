package com.tashfi.InventoryManagementSystem.customer.adapter.in.router;

import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.customer.adapter.in.handler.CustomerHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class CustomerRouter {

    @Bean
    public RouterFunction<ServerResponse> customerRoutes(CustomerHandler customerHandler) {
        return RouterFunctions.route()
                .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), builder -> builder
                        .GET(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL), customerHandler::getAllCustomers)
                        .POST(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_REGISTER_URL), customerHandler::registerCustomer)
                        .POST(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_LOGIN_URL), customerHandler::loginCustomer)
                ).build();
    }
}