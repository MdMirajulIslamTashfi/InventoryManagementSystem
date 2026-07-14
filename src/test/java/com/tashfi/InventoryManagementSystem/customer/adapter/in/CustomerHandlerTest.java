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
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.ProfileRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.response.*;
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
                .firstName("John")
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
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 24))
                .address("123 Dhaka Road, Mirpur")
                .contact("01712345678")
                .email("john@gmail.com")
                .password("john123")
                .build();
    }

    private ProfileRequestDto buildProfileDto(Customer customer) {
        return ProfileRequestDto.builder()
                .id(customer.getId().toString())
                .name(customer.getFirstName() + " " + customer.getLastName())
                // Emulate the masking behavior for the mock response
                .email("j***ohn@gmail.com")
                .mobile("*******5678")
                .gender(customer.getGender())
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_REGISTER_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_REGISTER_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_REGISTER_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_LOGIN_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_LOGIN_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_LOGIN_URL))
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
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_LOGIN_URL))
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

    // GET /api/customers/profile/{id}
    @Nested
    @DisplayName("GET /api/customers/{id}")
    class GetCustomerById {

        @Test
        @DisplayName("returns 200 with the customer when found")
        void returns200WhenFound() {
            // 1. Generate an underlying domain customer first
            Customer customer = buildCustomer();

            // 2. Map it to the Profile DTO via our helper (containing masked values)
            ProfileRequestDto profile = buildProfileDto(customer);

            ProfileResponseDto response = ProfileResponseDto.builder()
                    .message("Customer profile fetched successfully")
                    .profileData(profile)
                    .build();

            when(customerUseCase.findCustomerById(any(UUID.class))).thenReturn(Mono.just(response));

            webTestClient.get()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_PROFILE_URL).concat("/").concat(profile.getId()))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Customer profile fetched successfully")
                    .jsonPath("$.profileData.id").isEqualTo(profile.getId())
                    .jsonPath("$.profileData.name").isEqualTo("John Doe")
                    .jsonPath("$.profileData.email").isEqualTo("j***ohn@gmail.com")
                    .jsonPath("$.profileData.mobile").isEqualTo("*******5678");
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void returns404WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(customerUseCase.findCustomerById(any(UUID.class)))
                    .thenReturn(Mono.error(new CustomerNotFoundException("No customer found with id: " + id)));

            webTestClient.get()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_PROFILE_URL).concat("/").concat(id.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Not Found")
                    .jsonPath("$.message").isEqualTo("No customer found with id: " + id);
        }
    }

    // PUT /api/customers/update/{id}
    @Nested
    @DisplayName("PUT /api/customers/update/{id}")
    class UpdateCustomer {

        @Test
        @DisplayName("returns 200 with the updated customer")
        void returns200OnSuccess() {
            Customer customer = buildCustomer();
            customer.setAddress("New Address, Banani");
            CustomerSingleResponseDto response = CustomerSingleResponseDto.builder()
                    .message("Customer updated successfully")
                    .customerData(customer)
                    .build();

            when(customerUseCase.updateCustomer(any(UUID.class), any(Customer.class)))
                    .thenReturn(Mono.just(response));

            webTestClient.put()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_UPDATE_URL).concat("/").concat(customer.getId().toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Customer.builder().address("New Address, Banani").build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("Customer updated successfully")
                    .jsonPath("$.customerData.address").isEqualTo("New Address, Banani");
        }

        @Test
        @DisplayName("returns 400 when validation fails")
        void returns400OnValidationError() {
            UUID id = UUID.randomUUID();
            when(customerUseCase.updateCustomer(any(UUID.class), any(Customer.class)))
                    .thenReturn(Mono.error(new ValidationException("Name format is invalid")));

            webTestClient.put()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_UPDATE_URL).concat("/").concat(id.toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Customer.builder().firstName("John123").build())
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Validation Error")
                    .jsonPath("$.message").isEqualTo("Name format is invalid");
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void returns404WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(customerUseCase.updateCustomer(any(UUID.class), any(Customer.class)))
                    .thenReturn(Mono.error(new CustomerNotFoundException("No customer found with id: " + id)));

            webTestClient.put()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_UPDATE_URL).concat("/").concat(id.toString()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Customer.builder().address("Some Address").build())
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Not Found")
                    .jsonPath("$.message").isEqualTo("No customer found with id: " + id);
        }
    }

    // DELETE /api/customers/delete/{id}
    @Nested
    @DisplayName("DELETE /api/customers/delete/{id}")
    class DeleteCustomer {

        @Test
        @DisplayName("returns 204 when customer deleted")
        void returns204OnSuccess() {
            UUID id = UUID.randomUUID();
            when(customerUseCase.deleteCustomer(any(UUID.class))).thenReturn(Mono.empty());

            webTestClient.delete()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_DELETE_URL).concat("/").concat(id.toString()))
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }

        @Test
        @DisplayName("returns 404 when customer not found")
        void returns404WhenNotFound() {
            UUID id = UUID.randomUUID();
            when(customerUseCase.deleteCustomer(any(UUID.class)))
                    .thenReturn(Mono.error(new CustomerNotFoundException("No customer found with id: " + id)));

            webTestClient.delete()
                    .uri(RouterName.BASE_URL.concat(RouterName.CUSTOMER_BASE_URL).concat(RouterName.CUSTOMER_DELETE_URL).concat("/").concat(id.toString()))
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Not Found")
                    .jsonPath("$.message").isEqualTo("No customer found with id: " + id);
        }
    }
}