package com.tashfi.InventoryManagementSystem.customer.adapter.in.handler;

import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerHandler {

    private final CustomerUseCase customerUseCase;
    private final GlobalExceptionHandler exceptionHandler;

    public Mono<ServerResponse> getAllCustomers(ServerRequest request) {
        log.info("Received request to fetch all customers");
        return customerUseCase.findAllCustomers()
                .flatMap(response -> {
                    log.info("Successfully returning all customers");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch all customers: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> registerCustomer(ServerRequest request) {
        log.info("Received request to register a new customer");
        return request.bodyToMono(CustomerRegistrationRequestDto.class)
                .flatMap(customerUseCase::registerCustomer)
                .flatMap(response -> {
                    log.info("Customer registration request completed successfully");
                    return ServerResponse.status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to register customer: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> loginCustomer(ServerRequest request) {
        log.info("Received login request");
        return request.bodyToMono(CustomerLoginRequestDto.class)
                .flatMap(customerUseCase::loginCustomer)
                .flatMap(response -> {
                    log.info("Login request completed successfully");
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ex -> {
                    log.error("Login request failed: {}", ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> getCustomerById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to fetch customer by id: {}", id);
        return customerUseCase.findCustomerById(id)
                .flatMap(response -> {
                    log.info("Successfully returning customer with id: {}", id);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to fetch customer by id [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> updateCustomer(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to update customer with id: {}", id);
        return request.bodyToMono(Customer.class)
                .flatMap(customer -> customerUseCase.updateCustomer(id, customer))
                .flatMap(response -> {
                    log.info("Successfully updated customer with id: {}", id);
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response);
                })
                .onErrorResume(ex -> {
                    log.error("Failed to update customer [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }

    public Mono<ServerResponse> deleteCustomer(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        log.info("Received request to delete customer with id: {}", id);
        return customerUseCase.deleteCustomer(id)
                .then(Mono.defer(() -> {
                    log.info("Successfully deleted customer with id: {}", id);
                    return ServerResponse.noContent().build();
                }))
                .onErrorResume(ex -> {
                    log.error("Failed to delete customer [{}]: {}", id, ex.getMessage(), ex);
                    return exceptionHandler.handle(ex)
                            .flatMap(err -> ServerResponse.status(err.getStatus())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(err));
                });
    }
}