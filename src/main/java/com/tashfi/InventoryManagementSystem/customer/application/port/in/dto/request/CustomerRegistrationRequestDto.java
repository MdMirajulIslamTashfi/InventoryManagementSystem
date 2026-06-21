package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request;

import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationRequestDto {
    private String firstName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private String contact;
    private String email;
    private String password;
}