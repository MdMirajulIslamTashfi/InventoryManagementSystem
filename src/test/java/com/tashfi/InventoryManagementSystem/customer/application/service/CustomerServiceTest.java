package com.tashfi.InventoryManagementSystem.customer.application.service;

import com.tashfi.InventoryManagementSystem.core.exception.CustomerNotFoundException;
import com.tashfi.InventoryManagementSystem.core.exception.DuplicateEmailException;
import com.tashfi.InventoryManagementSystem.core.exception.ValidationException;
import com.tashfi.InventoryManagementSystem.core.util.JwtUtil;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerLoginRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.in.dto.request.CustomerRegistrationRequestDto;
import com.tashfi.InventoryManagementSystem.customer.application.port.out.CustomerPersistencePort;
import com.tashfi.InventoryManagementSystem.customer.domain.Customer;
import com.tashfi.InventoryManagementSystem.core.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock private CustomerPersistencePort customerPersistencePort;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    private CustomerService service;

    private CustomerRegistrationRequestDto validRegistrationRequest;
    private CustomerLoginRequestDto validLoginRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        service = new CustomerService(
                customerPersistencePort,
                passwordEncoder,
                jwtUtil
        );

        validRegistrationRequest = CustomerRegistrationRequestDto.builder()
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 24))
                .address("123 Dhaka Road, Mirpur")
                .contact("01712345678")
                .email("john@gmail.com")
                .password("john123")
                .build();

        validLoginRequest = CustomerLoginRequestDto.builder()
                .email("john@gmail.com")
                .password("john123")
                .build();

        savedCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1995, 3, 24))
                .address("123 Dhaka Road, Mirpur")
                .contact("01712345678")
                .email("john@gmail.com")
                .password("$2a$10$hashedpassword")
                .build();
    }

    @Nested
    @DisplayName("findAllCustomers()")
    class FindAllCustomers {

        @Test
        @DisplayName("returns all customers with message and correct count")
        void returnsAllCustomers() {
            Customer customer2 = Customer.builder()
                    .id(UUID.randomUUID())
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane@gmail.com")
                    .build();

            when(customerPersistencePort.findAllCustomers())
                    .thenReturn(Flux.just(savedCustomer, customer2));

            StepVerifier.create(service.findAllCustomers())
                    .assertNext(response -> {
                        assertThat(response.getMessage()).isEqualTo("Customers fetched successfully");
                        assertThat(response.getTotalRecords()).isEqualTo(2);
                        assertThat(response.getCustomerData()).hasSize(2);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns empty list with count 0 when no customers exist")
        void returnsEmptyList() {
            when(customerPersistencePort.findAllCustomers()).thenReturn(Flux.empty());

            StepVerifier.create(service.findAllCustomers())
                    .assertNext(response -> {
                        assertThat(response.getTotalRecords()).isEqualTo(0);
                        assertThat(response.getCustomerData()).isEmpty();
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("returns correct email for single customer")
        void returnsSingleCustomer() {
            when(customerPersistencePort.findAllCustomers())
                    .thenReturn(Flux.just(savedCustomer));

            StepVerifier.create(service.findAllCustomers())
                    .assertNext(response -> {
                        assertThat(response.getTotalRecords()).isEqualTo(1);
                        assertThat(response.getCustomerData().get(0).getEmail())
                                .isEqualTo("john@gmail.com");
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("registerCustomer() — success")
    class RegisterCustomerSuccess {

        @Test
        @DisplayName("successfully registers a valid customer")
        void registersValidCustomer() {
            when(customerPersistencePort.existsByEmail("john@gmail.com")).thenReturn(Mono.just(false));
            when(passwordEncoder.encode("john123")).thenReturn("$2a$10$hashedpassword");
            when(customerPersistencePort.saveCustomer(any(Customer.class))).thenReturn(Mono.just(savedCustomer));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .assertNext(response -> {
                        assertThat(response.getMessage()).isEqualTo("Customer registered successfully");
                        assertThat(response.getCustomerData().getEmail()).isEqualTo("john@gmail.com");
                    })
                    .verifyComplete();

            verify(passwordEncoder).encode("john123");
            verify(customerPersistencePort).saveCustomer(any(Customer.class));
        }

        @Test
        @DisplayName("encodes password before saving — never stores plain text")
        void passwordIsEncodedBeforeSaving() {
            when(customerPersistencePort.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(passwordEncoder.encode("john123")).thenReturn("$2a$10$hashedpassword");
            when(customerPersistencePort.saveCustomer(any())).thenReturn(Mono.just(savedCustomer));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .assertNext(response -> assertThat(response).isNotNull())
                    .verifyComplete();

            verify(passwordEncoder).encode("john123");
        }

        @Test
        @DisplayName("does not save when email already exists")
        void doesNotSaveOnDuplicateEmail() {
            when(customerPersistencePort.existsByEmail("john@gmail.com")).thenReturn(Mono.just(true));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof DuplicateEmailException)
                    .verify();

            verify(customerPersistencePort, never()).saveCustomer(any());
        }
    }

    @Nested
    @DisplayName("registerCustomer() — duplicate email")
    class RegisterCustomerDuplicateEmail {

        @Test
        @DisplayName("throws DuplicateEmailException with email in message")
        void throwsDuplicateEmailException() {
            when(customerPersistencePort.existsByEmail("john@gmail.com")).thenReturn(Mono.just(true));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex ->
                            ex instanceof DuplicateEmailException &&
                                    ex.getMessage().contains("john@gmail.com"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("registerCustomer() — name validation")
    class RegisterCustomerNameValidation {

        @Test
        @DisplayName("throws ValidationException for first name with numbers")
        void throwsOnFirstNameWithNumbers() {
            validRegistrationRequest.setFirstName("John123");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Name format is invalid"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for first name with special characters")
        void throwsOnFirstNameWithSpecialChars() {
            validRegistrationRequest.setFirstName("John@Doe");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null first name")
        void throwsOnNullFirstName() {
            validRegistrationRequest.setFirstName(null);
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for last name with numbers")
        void throwsOnLastNameWithNumbers() {
            validRegistrationRequest.setLastName("Doe99");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Name format is invalid"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for Last name with special characters")
        void throwsOnLastNameWithSpecialChars() {
            validRegistrationRequest.setLastName("@Doe");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null last name")
        void throwsOnNullLastName() {
            validRegistrationRequest.setLastName(null);
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("accepts name with spaces")
        void acceptsNameWithSpaces() {
            validRegistrationRequest.setFirstName("Mary Ann");
            when(customerPersistencePort.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(customerPersistencePort.saveCustomer(any())).thenReturn(Mono.just(savedCustomer));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .assertNext(r -> assertThat(r).isNotNull())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("registerCustomer() — email validation")
    class RegisterCustomerEmailValidation {

        @Test
        @DisplayName("throws ValidationException for email missing @")
        void throwsOnEmailMissingAt() {
            validRegistrationRequest.setEmail("notanemail");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Email format is invalid"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for email with uppercase letters")
        void throwsOnUppercaseEmail() {
            validRegistrationRequest.setEmail("John@Gmail.com");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for email missing domain")
        void throwsOnEmailMissingDomain() {
            validRegistrationRequest.setEmail("john@");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null email")
        void throwsOnNullEmail() {
            validRegistrationRequest.setEmail(null);
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("accepts valid email with dots and plus")
        void acceptsEmailWithDotsAndPlus() {
            validRegistrationRequest.setEmail("user.name+tag@example.org");
            when(customerPersistencePort.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(customerPersistencePort.saveCustomer(any())).thenReturn(Mono.just(savedCustomer));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .assertNext(r -> assertThat(r).isNotNull())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("registerCustomer() — contact validation")
    class RegisterCustomerContactValidation {

        @Test
        @DisplayName("throws ValidationException for contact shorter than 11 digits")
        void throwsOnShortContact() {
            validRegistrationRequest.setContact("0171234");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Contact must be exactly 11 digits"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for contact longer than 11 digits")
        void throwsOnLongContact() {
            validRegistrationRequest.setContact("017123456789");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for contact with letters")
        void throwsOnContactWithLetters() {
            validRegistrationRequest.setContact("0171234abcd");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null contact")
        void throwsOnNullContact() {
            validRegistrationRequest.setContact(null);
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }
    }

    @Nested
    @DisplayName("registerCustomer() — address validation")
    class RegisterCustomerAddressValidation {

        @Test
        @DisplayName("throws ValidationException for address with # character")
        void throwsOnHashInAddress() {
            validRegistrationRequest.setAddress("House #5, Mirpur");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Input format is invalid"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for address with $ character")
        void throwsOnDollarInAddress() {
            validRegistrationRequest.setAddress("Cost $100");
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null address")
        void throwsOnNullAddress() {
            validRegistrationRequest.setAddress(null);
            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("accepts address with allowed characters")
        void acceptsAddressWithAllowedChars() {
            validRegistrationRequest.setAddress("Flat-4, Block B, Dhaka");
            when(customerPersistencePort.existsByEmail(anyString())).thenReturn(Mono.just(false));
            when(passwordEncoder.encode(anyString())).thenReturn("hashed");
            when(customerPersistencePort.saveCustomer(any())).thenReturn(Mono.just(savedCustomer));

            StepVerifier.create(service.registerCustomer(validRegistrationRequest))
                    .assertNext(r -> assertThat(r).isNotNull())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("loginCustomer() — success")
    class LoginCustomerSuccess {

        @Test
        @DisplayName("returns token, email and expiry on successful login")
        void loginSuccess() {
            when(customerPersistencePort.findByEmail("john@gmail.com"))
                    .thenReturn(Mono.just(savedCustomer));
            when(passwordEncoder.matches("john123", "$2a$10$hashedpassword")).thenReturn(true);
            when(jwtUtil.generateToken("john@gmail.com")).thenReturn("jwt-token-abc");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);

            StepVerifier.create(service.loginCustomer(validLoginRequest))
                    .assertNext(response -> {
                        assertThat(response.getStatus()).isEqualTo("success");
                        assertThat(response.getMessage()).isEqualTo("Login successful");
                        assertThat(response.getToken()).isEqualTo("jwt-token-abc");
                        assertThat(response.getEmail()).isEqualTo("john@gmail.com");
                        assertThat(response.getExpiresIn()).isEqualTo(3600000L);
                    })
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("loginCustomer() — email validation")
    class LoginCustomerEmailValidation {

        @Test
        @DisplayName("throws ValidationException for invalid email format")
        void throwsOnInvalidEmailFormat() {
            CustomerLoginRequestDto request = CustomerLoginRequestDto.builder()
                    .email("not-valid").password("john123").build();
            StepVerifier.create(service.loginCustomer(request))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Email format is invalid"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for null email")
        void throwsOnNullEmail() {
            CustomerLoginRequestDto request = CustomerLoginRequestDto.builder()
                    .email(null).password("john123").build();
            StepVerifier.create(service.loginCustomer(request))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException for email with uppercase")
        void throwsOnUppercaseEmailLogin() {
            CustomerLoginRequestDto request = CustomerLoginRequestDto.builder()
                    .email("John@Gmail.com").password("john123").build();
            StepVerifier.create(service.loginCustomer(request))
                    .expectErrorMatches(ex -> ex instanceof ValidationException)
                    .verify();
        }
    }

    @Nested
    @DisplayName("loginCustomer() — credential errors")
    class LoginCustomerCredentialErrors {

        @Test
        @DisplayName("throws CustomerNotFoundException when email not registered")
        void throwsWhenEmailNotFound() {
            when(customerPersistencePort.findByEmail("john@gmail.com")).thenReturn(Mono.empty());
            StepVerifier.create(service.loginCustomer(validLoginRequest))
                    .expectErrorMatches(ex -> ex instanceof CustomerNotFoundException &&
                            ex.getMessage().contains("No account found with this email"))
                    .verify();
        }

        @Test
        @DisplayName("throws ValidationException when password does not match")
        void throwsOnWrongPassword() {
            when(customerPersistencePort.findByEmail("john@gmail.com"))
                    .thenReturn(Mono.just(savedCustomer));
            when(passwordEncoder.matches("wrongpassword", "$2a$10$hashedpassword")).thenReturn(false);

            CustomerLoginRequestDto request = CustomerLoginRequestDto.builder()
                    .email("john@gmail.com").password("wrongpassword").build();

            StepVerifier.create(service.loginCustomer(request))
                    .expectErrorMatches(ex -> ex instanceof ValidationException &&
                            ex.getMessage().equals("Invalid password"))
                    .verify();
        }

        @Test
        @DisplayName("does not generate token when password is wrong")
        void doesNotGenerateTokenOnWrongPassword() {
            when(customerPersistencePort.findByEmail("john@gmail.com"))
                    .thenReturn(Mono.just(savedCustomer));
            when(passwordEncoder.matches("wrong", "$2a$10$hashedpassword")).thenReturn(false);

            CustomerLoginRequestDto request = CustomerLoginRequestDto.builder()
                    .email("john@gmail.com").password("wrong").build();

            StepVerifier.create(service.loginCustomer(request))
                    .expectError(ValidationException.class)
                    .verify();

            verify(jwtUtil, never()).generateToken(anyString());
        }
    }
}