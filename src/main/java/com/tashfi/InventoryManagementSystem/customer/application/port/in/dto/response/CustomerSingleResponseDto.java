package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerSingleResponseDto {
    private String message;
    private Customer customerData;
}
