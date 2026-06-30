package com.tashfi.InventoryManagementSystem.customer.application.port.out;

import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerPersistencePort {
    Flux<Customer> findAllCustomers();
    Mono<Boolean> existsByEmail(String email);
    Mono<Customer> saveCustomer(Customer customer);
    Mono<Customer> findByEmail(String email);
    Mono<Customer> findById(UUID id);
    Mono<Boolean> existsById(UUID id);
    Mono<Void> deleteById(UUID id);
    Mono<Boolean> existsByContact(String contact);
}