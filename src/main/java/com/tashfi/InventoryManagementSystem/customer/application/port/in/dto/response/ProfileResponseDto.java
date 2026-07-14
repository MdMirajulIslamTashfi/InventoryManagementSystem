package com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response;

import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.ProfileRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponseDto {
        private String message;
        private ProfileRequestDto profileData;
}
