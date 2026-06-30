package com.tashfi.InventoryManagementSystem.customer.adapter.out.repository;

import com.tashfi.InventoryManagementSystem.customer.adapter.out.entity.CustomerEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerRepository extends R2dbcRepository<CustomerEntity, UUID> {
    Mono<CustomerEntity> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
    Mono<Boolean> existsByContact(String contact);
}