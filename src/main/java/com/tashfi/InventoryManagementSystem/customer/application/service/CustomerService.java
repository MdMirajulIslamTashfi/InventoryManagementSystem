package com.tashfi.InventoryManagementSystem.customer.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.DuplicateEmailException;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerRegistrationResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CustomerService implements CustomerUseCase {

    private final CustomerPersistencePort customerPersistencePort;
    // Add to CustomerService constructor:
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerPersistencePort customerPersistencePort, PasswordEncoder passwordEncoder) {
        this.customerPersistencePort = customerPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<CustomerResponseDto> findAllCustomers() {
        return customerPersistencePort.findAllCustomers()
                .collectList()
                .map(customers -> CustomerResponseDto.builder()
                        .message("Customers fetched successfully")
                        .totalRecords(customers.size())
                        .customerData(customers)
                        .build());
    }

    @Override
    public Mono<CustomerRegistrationResponseDto> registerCustomer(CustomerRegistrationRequestDto request) {
        return ValidationUtil.validateName(request.getFullName())
                .then(ValidationUtil.validateName(request.getLastName()))
                .then(ValidationUtil.validateEmail(request.getEmail()))
                .then(ValidationUtil.validateContact(request.getContact()))
                .then(ValidationUtil.validateInput(request.getAddress()))
                .then(customerPersistencePort.existsByEmail(request.getEmail()))
                .flatMap(exists -> {
                    if (exists)
                        return Mono.error(new DuplicateEmailException("Email already registered: " + request.getEmail()));

                    Customer customer = Customer.builder()
                            .fullName(request.getFullName())
                            .lastName(request.getLastName())
                            .gender(request.getGender())
                            .dateOfBirth(request.getDateOfBirth())
                            .address(request.getAddress())
                            .contact(request.getContact())
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .build();

                    return customerPersistencePort.saveCustomer(customer);
                })
                .map(saved -> CustomerRegistrationResponseDto.builder()
                        .message("Customer registered successfully")
                        .customerData(saved)
                        .build());
    }
}