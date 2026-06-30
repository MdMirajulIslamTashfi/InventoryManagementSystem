package com.tashfi.InventoryManagementSystem.customer.application.port.in;

import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerLoginResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerRegistrationResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerSingleResponseDto;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CustomerUseCase {
    Mono<CustomerResponseDto> findAllCustomers();
    Mono<CustomerRegistrationResponseDto> registerCustomer(CustomerRegistrationRequestDto request);
    Mono<CustomerLoginResponseDto> loginCustomer(CustomerLoginRequestDto request);
    Mono<CustomerSingleResponseDto> findCustomerById(UUID id);
    Mono<CustomerSingleResponseDto> updateCustomer(UUID id, Customer customer);
    Mono<Void> deleteCustomer(UUID id);
}