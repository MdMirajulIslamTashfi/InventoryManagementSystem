package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationResponseDto {
    private String message;
    private Customer customerData;
}