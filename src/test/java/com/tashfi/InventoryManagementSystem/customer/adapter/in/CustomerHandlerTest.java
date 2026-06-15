package com.tashfi.InventoryManagementSystem.customer.adapter.in;

import com.tashfi.InventoryManagementSystem.core.exception.CustomerNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateEmailException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.exception.handler.GlobalExceptionHandler;
import com.tashfi.InventoryManagementSystem.core.routers.RouterName;
import com.tashfi.InventoryManagementSystem.customer.adapter.in.handler.CustomerHandler;
import com.tashfi.InventoryManagementSystem.customer.adapter.in.router.CustomerRouter;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.CustomerUseCase;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerLoginResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerRegistrationResponseDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.CustomerResponseDto;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Handler Test")
public class CustomerHandlerTest {

    @Mock
    private CustomerUseCase customerUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();
        CustomerHandler handler = new CustomerHandler(customerUseCase, exceptionHandler);
        CustomerRouter router = new CustomerRouter();
        webTestClient = WebTestClient
                .bindToRouterFunction(router.customerRoutes(handler))
                .build();
    }

    private Customer buildCustomer() {
        return Customer.builder()
                .id(UUID.randomUUID())
                .fullName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 24))
                .address("123 Dhaka Road, Mirpur")
                .contact("01712345678")
                .email("john@gmail.com")
                .build();
    }

    private CustomerRegistrationRequestDto buildRegistrationRequest() {
        return CustomerRegistrationRequestDto.builder()
                .fullName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 24))
                .address("123 Dhaka Road, Mirpur")
                .contact("01712345678")
                .email("john@gmail.com")
                .password("john123")
                .build();
    }

    // GET /api/customers
    @Nested
    @DisplayName("GET /api/customers")
    class GetAllCustomers {

        @Test
        @DisplayName("returns 200 with customer list")
        void returns200WithCustomerList() {
            CustomerResponseDto response = CustomerResponseDto.builder()
                    .message("Customers fetched successfully")
                    .totalRecords(1)
                    .customerData(List.of(buildCustomer()))
                    .build();

            when(customerUseCase.findAllCustomers()).thenReturn(Mono.just(response));

            webTestClient.get()
                    .uri(RouterName.CUSTOMER_BASE_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Customers fetched successfully")
                    .jsonPath("$.totalRecords").isEqualTo(1)
                    .jsonPath("$.customerData[0].email").isEqualTo("john@gmail.com");
        }

        @Test
        @DisplayName("returns 200 with empty list when no customers")
        void returns200WithEmptyList() {
            CustomerResponseDto response = CustomerResponseDto.builder()
                    .message("Customers fetched successfully")
                    .totalRecords(0)
                    .customerData(List.of())
                    .build();

            when(customerUseCase.findAllCustomers()).thenReturn(Mono.just(response));

            webTestClient.get()
                    .uri(RouterName.CUSTOMER_BASE_URL)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.totalRecords").isEqualTo(0)
                    .jsonPath("$.customerData").isArray();
        }
    }

    // POST /api/customers/register
    @Nested
    @DisplayName("POST /api/customers/register")
    class RegisterCustomer {

        @Test
        @DisplayName("returns 201 on successful registration")
        void returns201OnSuccess() {
            CustomerRegistrationResponseDto response = CustomerRegistrationResponseDto.builder()
                    .message("Customer registered successfully")
                    .customerData(buildCustomer())
                    .build();

            when(customerUseCase.registerCustomer(any(CustomerRegistrationRequestDto.class)))
                    .thenReturn(Mono.just(response));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(buildRegistrationRequest())
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Customer registered successfully")
                    .jsonPath("$.customerData.email").isEqualTo("john@gmail.com");
        }

        @Test
        @DisplayName("returns 400 when validation fails")
        void returns400OnValidationError() {
            when(customerUseCase.registerCustomer(any()))
                    .thenReturn(Mono.error(new ValidationException("Email format is invalid")));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(buildRegistrationRequest())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation Error")
                    .jsonPath("$.message").isEqualTo("Email format is invalid");
        }

        @Test
        @DisplayName("returns 409 when email already registered")
        void returns409OnDuplicateEmail() {
            when(customerUseCase.registerCustomer(any()))
                    .thenReturn(Mono.error(new DuplicateEmailException("Email already registered: john@gmail.com")));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_REGISTER_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(buildRegistrationRequest())
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Duplicate Entry")
                    .jsonPath("$.message").isEqualTo("Email already registered: john@gmail.com");
        }
    }

    // POST /api/customers/login
    @Nested
    @DisplayName("POST /api/customers/login")
    class LoginCustomer {

        @Test
        @DisplayName("returns 200 with token on successful login")
        void returns200WithToken() {
            CustomerLoginResponseDto response = CustomerLoginResponseDto.builder()
                    .status("success")
                    .message("Login successful")
                    .token("jwt-token-abc")
                    .email("john@gmail.com")
                    .expiresIn(3600000L)
                    .build();

            when(customerUseCase.loginCustomer(any(CustomerLoginRequestDto.class)))
                    .thenReturn(Mono.just(response));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(CustomerLoginRequestDto.builder()
                            .email("john@gmail.com")
                            .password("john123")
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.status").isEqualTo("success")
                    .jsonPath("$.token").isEqualTo("jwt-token-abc")
                    .jsonPath("$.email").isEqualTo("john@gmail.com")
                    .jsonPath("$.expiresIn").isEqualTo(3600000);
        }

        @Test
        @DisplayName("returns 400 when email format is invalid")
        void returns400OnInvalidEmail() {
            when(customerUseCase.loginCustomer(any()))
                    .thenReturn(Mono.error(new ValidationException("Email format is invalid")));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(CustomerLoginRequestDto.builder()
                            .email("invalid")
                            .password("john123")
                            .build())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation Error")
                    .jsonPath("$.message").isEqualTo("Email format is invalid");
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void returns404WhenNotFound() {
            when(customerUseCase.loginCustomer(any()))
                    .thenReturn(Mono.error(new CustomerNotFoundException("No account found with this email")));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(CustomerLoginRequestDto.builder()
                            .email("john@gmail.com")
                            .password("john123")
                            .build())
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Not Found")
                    .jsonPath("$.message").isEqualTo("No account found with this email");
        }

        @Test
        @DisplayName("returns 400 when password is wrong")
        void returns400OnWrongPassword() {
            when(customerUseCase.loginCustomer(any()))
                    .thenReturn(Mono.error(new ValidationException("Invalid password")));

            webTestClient.post()
                    .uri(RouterName.CUSTOMER_LOGIN_URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(CustomerLoginRequestDto.builder()
                            .email("john@gmail.com")
                            .password("wrong password")
                            .build())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation Error")
                    .jsonPath("$.message").isEqualTo("Invalid password");
        }
    }
}