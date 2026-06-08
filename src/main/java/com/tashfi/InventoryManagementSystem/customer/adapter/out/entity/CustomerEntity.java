package com.tashfi.InventoryManagementSystem.customer.adapter.out.entity;

import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class CustomerEntity {
    @Id
    private UUID id;

    @Column("full_name")
    private String fullName;

    @Column("last_name")
    private String lastName;

    private Gender gender;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;
    private String address;
    private String contact;
    private String email;
    private String password;

}
