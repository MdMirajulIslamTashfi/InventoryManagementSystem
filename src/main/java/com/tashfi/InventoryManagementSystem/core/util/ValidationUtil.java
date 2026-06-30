package com.tashfi.InventoryManagementSystem.core.util;

import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import reactor.core.publisher.Mono;

public class ValidationUtil {

    private static final String NAME_PATTERN = "^[a-zA-Z ]+$";
    private static final String EMAIL_PATTERN = "^[0-9a-z._+-]+@[a-z0-9]+\\.[a-z0-9]{2,20}$";
    private static final String INPUT_PATTERN = "^[a-zA-Z0-9._@, +-]+$";
    private static final String CONTACT_PATTERN = "^\\+[1-9]\\d{7,14}$";

    private ValidationUtil() {
    }

    public static Mono<String> validateName(String name) {
        if (name == null || !name.matches(NAME_PATTERN))
            return Mono.error(new ValidationException("Name format is invalid"));
        return Mono.just(name);
    }

    public static Mono<String> validateEmail(String email) {
        if (email == null || !email.matches(EMAIL_PATTERN))
            return Mono.error(new ValidationException("Email format is invalid"));
        return Mono.just(email);
    }

    public static Mono<String> validateInput(String input) {
        if (input == null || !input.matches(INPUT_PATTERN))
            return Mono.error(new ValidationException("Input format is invalid"));
        return Mono.just(input);
    }

    public static Mono<String> validateContact(String contact) {
        if (contact == null || !contact.matches(CONTACT_PATTERN))
            return Mono.error(new ValidationException("Contact must be start with + sign and exactly 7-14 digits"));
        return Mono.just(contact);
    }
}