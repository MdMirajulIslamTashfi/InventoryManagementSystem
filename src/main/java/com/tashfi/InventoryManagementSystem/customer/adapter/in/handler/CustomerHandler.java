package com.tashfi.InventoryManagementSystem.customer.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static java.lang.System.err;

@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerUseCase customerUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        return customerUseCase.findAllCustomers()
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(err)));
    }

    public Mono<ServerResponse> registerCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerRegistrationRequestDto.class)
                .flatMap(customerUseCase::registerCustomer)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(err)));
    }

    public Mono<ServerResponse> loginCustomer(ServerRequest request) {
        return request.bodyToMono(CustomerLoginRequestDto.class)
                .flatMap(customerUseCase::loginCustomer)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(err)));
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return customerUseCase.findCustomerById(id)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(err)));
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(Customer.class)
                .flatMap(customer -> customerUseCase.updateCustomer(id, customer))
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response))
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(err)));
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return customerUseCase.deleteCustomer(id)
                .then(ServerResponse.noContent().build())
                .onErrorResume(ex -> exceptionHandler.handle(ex)
                        .flatMap(err -> ServerResponse.status(err.getStatus())
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(err)));
    }
}