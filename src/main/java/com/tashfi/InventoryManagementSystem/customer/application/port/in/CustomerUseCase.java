package com.tashfi.InventoryManagementSystem.customer.application.port.in;

import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.*;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerUseCase {
    Mono<CustomerResponseDto> findAllCustomers();
    Mono<CustomerRegistrationResponseDto> registerCustomer(CustomerRegistrationRequestDto request);
    Mono<CustomerLoginResponseDto> loginCustomer(CustomerLoginRequestDto request);
    Mono<ProfileResponseDto> findCustomerById(UUID id);
    Mono<CustomerSingleResponseDto> updateCustomer(UUID id, Customer customer);
    Mono<Void> deleteCustomer(UUID id);
}