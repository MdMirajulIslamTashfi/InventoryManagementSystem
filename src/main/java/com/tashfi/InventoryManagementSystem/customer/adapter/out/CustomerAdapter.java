package com.tashfi.InventoryManagementSystem.customer.adapter.out;

import com.tashfi.InventoryManagementSystem.customer.adapter.out.entity.CustomerEntity;
import com.tashfi.InventoryManagementSystem.customer.adapter.out.repository.CustomerRepository;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

    @Override
    public Mono<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Mono<Customer> findById(UUID id) {
        return customerRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return customerRepository.existsById(id);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return customerRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> existsByContact(String contact) {
        return customerRepository.existsByContact(contact);
    }

    // ── mappers ──────────────────────────────────────────────
    private Customer toDomain(CustomerEntity entity) {
        return Customer.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .gender(entity.getGender())
                .dateOfBirth(entity.getDateOfBirth())
                .address(entity.getAddress())
                .contact(entity.getContact())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedBy(entity.getUpdatedBy())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .address(customer.getAddress())
                .contact(customer.getContact())
                .email(customer.getEmail())
                .password(customer.getPassword())
                .createdBy(customer.getCreatedBy())
                .createdAt(customer.getCreatedAt())
                .updatedBy(customer.getUpdatedBy())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}