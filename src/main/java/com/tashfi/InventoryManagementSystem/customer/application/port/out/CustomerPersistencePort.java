package com.tashfi.InventoryManagementSystem.customer.application.port.out;

import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerPersistencePort {
    Flux<Customer> findAllCustomers();

    Mono<Boolean> existsByEmail(String email);

    Mono<Customer> saveCustomer(Customer customer);

    Mono<Customer> findByEmail(String email);
}