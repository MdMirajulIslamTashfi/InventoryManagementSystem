package com.tashfi.InventoryManagementSystem.customer.adapter.out;

import com.tashfi.InventoryManagementSystem.customer.adapter.out.entity.CustomerEntity;
import com.tashfi.InventoryManagementSystem.customer.adapter.out.repository.CustomerRepository;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomerAdapter implements CustomerPersistencePort {

    private final CustomerRepository customerRepository;

    @Override
    public Flux<Customer> findAllCustomers() {
        return customerRepository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public Mono<Customer> saveCustomer(Customer customer) {
        return customerRepository.save(toEntity(customer)).map(this::toDomain);
    }

    // ── mappers ──────────────────────────────────────────────

    private Customer toDomain(CustomerEntity entity) {
        return Customer.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .lastName(entity.getLastName())
                .gender(entity.getGender())
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .contact(entity.getContact())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .build();
    }

    private CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .lastName(customer.getLastName())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .address(customer.getAddress())
                .contact(customer.getContact())
                .email(customer.getEmail())
                .password(customer.getPassword())
                .build();
    }
}