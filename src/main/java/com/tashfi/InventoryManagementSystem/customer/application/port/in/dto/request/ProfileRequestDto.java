package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request;

import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {
    private String id;
    private String name;
    private String email;    // masked, e.g. "***iyad@example.com"
    private String mobile;   // masked, e.g. "**********0000"
    private Gender gender;
    private Boolean isActive;
}
