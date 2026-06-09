package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerLoginResponseDto {
    private String status;
    private String message;
    private String token;
    private String email;
    private long expiresIn;
}
