package com.tashfi.InventoryManagementSystem.customer.adapter.out.entity;

import com.tashfi.InventoryManagementSystem.core.enums.CreatedBy;
import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer")
public class CustomerEntity {

    @Id
    private UUID id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    private Gender gender;

    @Column("date_of_birth")
    private LocalDate dateOfBirth;

    private String address;
    private String contact;
    private String email;
    private String password;

    @Column("created_by")
    private CreatedBy createdBy;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_by")
    private CreatedBy updatedBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}