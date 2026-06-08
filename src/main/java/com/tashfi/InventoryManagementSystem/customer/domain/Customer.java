package com.tashfi.InventoryManagementSystem.customer.domain;

import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private UUID id;
    private String fullName;
    private String lastName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String address;
    private String contact;
    private String email;
    private String password;
}
