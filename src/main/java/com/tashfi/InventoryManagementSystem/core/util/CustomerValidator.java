package com.tashfi.InventoryManagementSystem.core.util;

import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CustomerValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern CONTACT_PATTERN =
            Pattern.compile("^\\d{11}$");

    public static List<String> validate(CustomerRegistrationRequestDto dto) {
        List<String> errors = new ArrayList<>();

        if (isBlank(dto.getFullName()))
            errors.add("Full name is required");

        if (isBlank(dto.getLastName()))
            errors.add("Last name is required");

        if (dto.getGender() == null)
            errors.add("Gender is required");

        if (dto.getDateOfBirth() == null)
            errors.add("Date of birth is required");

        if (isBlank(dto.getAddress()))
            errors.add("Address is required");
        else if (dto.getAddress().length() > 255)
            errors.add("Address must not exceed 255 characters");

        if (isBlank(dto.getContact()))
            errors.add("Contact is required");
        else if (!CONTACT_PATTERN.matcher(dto.getContact()).matches())
            errors.add("Contact must be exactly 11 digits");

        if (isBlank(dto.getEmail()))
            errors.add("Email is required");
        else if (!EMAIL_PATTERN.matcher(dto.getEmail()).matches())
            errors.add("Email format is invalid");

        if (isBlank(dto.getPassword()))
            errors.add("Password is required");
        else if (dto.getPassword().length() < 6)
            errors.add("Password must be at least 6 characters");

        return errors;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}