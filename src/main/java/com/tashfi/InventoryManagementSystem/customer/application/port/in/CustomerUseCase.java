package com.tashfi.InventoryManagementSystem.customer.application.port.in;

import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerRegistrationResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerResponseDto;
import reactor.core.publisher.Mono;

public interface CustomerUseCase {
    Mono<CustomerResponseDto> findAllCustomers();
    Mono<CustomerRegistrationResponseDto> registerCustomer(CustomerRegistrationRequestDto request);
}