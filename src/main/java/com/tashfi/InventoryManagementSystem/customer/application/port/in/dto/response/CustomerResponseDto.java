package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerResponseDto {
    private String message;
    private Integer totalRecords;
    private List<Customer> customerData;
}