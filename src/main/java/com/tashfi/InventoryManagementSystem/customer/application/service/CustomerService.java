package com.tashfi.InventoryManagementSystem.customer.application.service;

import com.tashfi.InventoryManagementSystem.core.enums.CreatedBy;
import com.tashfi.InventoryManagementSystem.core.exception.CustomerNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateContactException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateEmailException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.util.JwtUtil;
import com.tashfi.InventoryManagementSystem.core.util.MaskUtil;
import com.tashfi.InventoryManagementSystem.core.util.ValidationUtil;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.ProfileRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.*;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService implements CustomerUseCase {

    private final CustomerPersistencePort customerPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<CustomerResponseDto> findAllCustomers() {
        log.info("Fetching all customers");
        return customerPersistencePort.findAllCustomers()
                .collectList()
                .map(customers -> {
                    log.info("Fetched {} customers successfully", customers.size());
                    return CustomerResponseDto.builder()
                            .message("Customers fetched successfully")
                            .totalRecords(customers.size())
                            .customerData(customers)
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching all customers: {}", ex.getMessage(), ex));
    }

    @Override
    public Mono<CustomerRegistrationResponseDto> registerCustomer(CustomerRegistrationRequestDto request) {
        log.info("Registering customer with email: {}", request.getEmail());
        return ValidationUtil.validateName(request.getFirstName())
                .flatMap(__ -> ValidationUtil.validateName(request.getLastName()))
                .flatMap(__ -> ValidationUtil.validateEmail(request.getEmail()))
                .flatMap(__ -> ValidationUtil.validateContact(request.getContact()))
                .flatMap(__ -> ValidationUtil.validateInput(request.getAddress()))
                .doOnError(ex -> log.warn("Validation failed while registering customer [{}]: {}",
                        request.getEmail(), ex.getMessage()))
                .flatMap(__ -> customerPersistencePort.existsByEmail(request.getEmail()))
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Registration failed - duplicate email: {}", request.getEmail());
                        return Mono.error(new DuplicateEmailException("Email already registered: " + request.getEmail()));
                    }
                    return customerPersistencePort.existsByContact(request.getContact());
                })
                .flatMap(exists -> {
                    if (exists) {
                        log.warn("Registration failed - duplicate contact: {}", request.getContact());
                        return Mono.error(new DuplicateContactException("Contact already exists: " + request.getContact()));
                    }

                    log.debug("Building new customer entity for email: {}", request.getEmail());
                    Customer customer = Customer.builder()
                            .firstName(request.getFirstName())
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
                .map(saved -> {
                    log.info("Customer registered successfully with id: {}", saved.getId());
                    return CustomerRegistrationResponseDto.builder()
                            .message("Customer registered successfully")
                            .customerData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while registering customer [{}]: {}",
                        request.getEmail(), ex.getMessage(), ex));
    }

    @Override
    public Mono<CustomerLoginResponseDto> loginCustomer(CustomerLoginRequestDto request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return ValidationUtil.validateEmail(request.getEmail())
                .flatMap(__ -> customerPersistencePort.findByEmail(request.getEmail()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login failed - no account found for email: {}", request.getEmail());
                    return Mono.error(new CustomerNotFoundException("No account found with this email"));
                }))
                .flatMap(customer -> {
                    if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
                        log.warn("Login failed - invalid password for email: {}", request.getEmail());
                        return Mono.error(new ValidationException("Invalid password"));
                    }

                    String token = jwtUtil.generateToken(customer.getEmail());
                    long expiresIn = jwtUtil.getExpiration();

                    log.info("Login successful for email: {}", customer.getEmail());
                    return Mono.just(CustomerLoginResponseDto.builder()
                            .status("success")
                            .message("Login successful")
                            .token(token)
                            .email(customer.getEmail())
                            .expiresIn(expiresIn)
                            .build());
                })
                .doOnError(ex -> log.error("Error during login for email [{}]: {}",
                        request.getEmail(), ex.getMessage(), ex));
    }

    @Override
    public Mono<ProfileResponseDto> findCustomerById(UUID id) {
        log.info("Fetching customer by id: {}", id);
        return customerPersistencePort.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found for id: {}", id);
                    return Mono.error(new CustomerNotFoundException("No customer found with id: " + id));
                }))
                .map(customer -> {
                    log.info("Customer fetched successfully for id: {}", id);

                    // 1. Map the customer entity to the ProfileDto and apply masking
                    ProfileRequestDto profile = ProfileRequestDto.builder()
                            .id(customer.getId().toString()) // Convert UUID to String if needed
                            .name(customer.getFirstName() + customer.getLastName())
                            .email(MaskUtil.maskEmail(customer.getEmail()))    // Masked Email
                            .mobile(MaskUtil.maskContact(customer.getContact())) // Masked Mobile
                            .gender(customer.getGender())
                            .build();

                    // 2. Wrap the ProfileDto inside your CustomerSingleResponseDto
                    return ProfileResponseDto.builder()
                            .message("Customer profile fetched successfully")
                            .profileData(profile) // Ensure customerData field in DTO can accept ProfileDto
                            .build();
                })
                .doOnError(ex -> log.error("Error while fetching customer by id [{}]: {}",
                        id, ex.getMessage(), ex));
    }

    @Override
    public Mono<CustomerSingleResponseDto> updateCustomer(UUID id, Customer customer) {
        log.info("Updating customer with id: {}", id);
        return customerPersistencePort.findById(id)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Update failed - no customer found for id: {}", id);
                    return Mono.error(new CustomerNotFoundException("No customer found with id: " + id));
                }))
                .flatMap(existing -> {
                    Mono<Void> validation = Mono.empty();

                    if (customer.getFirstName() != null)
                        validation = validation.then(ValidationUtil.validateName(customer.getFirstName())).then();
                    if (customer.getLastName() != null)
                        validation = validation.then(ValidationUtil.validateName(customer.getLastName())).then();
                    if (customer.getContact() != null)
                        validation = validation.then(ValidationUtil.validateContact(customer.getContact())).then();
                    if (customer.getAddress() != null)
                        validation = validation.then(ValidationUtil.validateInput(customer.getAddress())).then();

                    return validation
                            .doOnError(ex -> log.warn("Validation failed while updating customer [{}]: {}",
                                    id, ex.getMessage()))
                            .thenReturn(existing);
                })
                .flatMap(existing -> {
                    log.debug("Applying updated fields for customer id: {}", id);
                    if (customer.getFirstName() != null) existing.setFirstName(customer.getFirstName());
                    if (customer.getLastName() != null) existing.setLastName(customer.getLastName());
                    if (customer.getGender() != null) existing.setGender(customer.getGender());
                    if (customer.getDateOfBirth() != null) existing.setDateOfBirth(customer.getDateOfBirth());
                    if (customer.getAddress() != null) existing.setAddress(customer.getAddress());
                    if (customer.getContact() != null) existing.setContact(customer.getContact());
                    existing.setUpdatedBy(CreatedBy.EMAIL);
                    existing.setUpdatedAt(LocalDateTime.now());

                    return customerPersistencePort.saveCustomer(existing);
                })
                .map(saved -> {
                    log.info("Customer updated successfully for id: {}", id);
                    return CustomerSingleResponseDto.builder()
                            .message("Customer updated successfully")
                            .customerData(saved)
                            .build();
                })
                .doOnError(ex -> log.error("Error while updating customer [{}]: {}",
                        id, ex.getMessage(), ex));
    }

    @Override
    public Mono<Void> deleteCustomer(UUID id) {
        log.info("Deleting customer with id: {}", id);
        return customerPersistencePort.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("Delete failed - no customer found for id: {}", id);
                        return Mono.error(new CustomerNotFoundException("No customer found with id: " + id));
                    }
                    return customerPersistencePort.deleteById(id)
                            .doOnSuccess(v -> log.info("Customer deleted successfully for id: {}", id));
                })
                .doOnError(ex -> log.error("Error while deleting customer [{}]: {}",
                        id, ex.getMessage(), ex));
    }
}