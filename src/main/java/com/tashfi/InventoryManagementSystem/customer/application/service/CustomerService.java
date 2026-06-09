package com.tashfi.InventoryManagementSystem.customer.application.service;

import com.tashfi.InventoryManagementSystem.core.enums.CreatedBy;
import com.tashfi.InventoryManagementSystem.core.exception.CustomerNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateEmailException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.util.JwtUtil;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerLoginResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerRegistrationResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class CustomerService implements CustomerUseCase {

    private final CustomerPersistencePort customerPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public CustomerService(CustomerPersistencePort customerPersistencePort,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.customerPersistencePort = customerPersistencePort;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
                            .createdBy(CreatedBy.EMAIL)
                            .createdAt(LocalDateTime.now())
                            .build();

                    return customerPersistencePort.saveCustomer(customer);
                })
                .map(saved -> CustomerRegistrationResponseDto.builder()
                        .message("Customer registered successfully")
                        .customerData(saved)
                        .build());
    }

    @Override
    public Mono<CustomerLoginResponseDto> loginCustomer(CustomerLoginRequestDto request) {
        return ValidationUtil.validateEmail(request.getEmail())
                .then(customerPersistencePort.findByEmail(request.getEmail()))
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("No account found with this email")))
                .flatMap(customer -> {
                    if (!passwordEncoder.matches(request.getPassword(), customer.getPassword()))
                        return Mono.error(new ValidationException("Invalid password"));

                    String token = jwtUtil.generateToken(customer.getEmail());
                    long expiresIn = jwtUtil.getExpiration();

                    return Mono.just(CustomerLoginResponseDto.builder()
                            .status("success")
                            .message("Login successful")
                            .token(token)
                            .email(customer.getEmail())
                            .expiresIn(expiresIn)
                            .build());
                });
    }
}