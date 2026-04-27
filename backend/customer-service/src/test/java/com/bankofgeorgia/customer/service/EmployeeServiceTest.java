package com.bankofgeorgia.customer.service;

import com.bankofgeorgia.customer.dto.EmployeeLoginRequest;
import com.bankofgeorgia.customer.exception.AuthException;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.model.Employee;
import com.bankofgeorgia.customer.repository.CustomerRepository;
import com.bankofgeorgia.customer.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private EmployeeService employeeService;

    private Employee admin;

    @BeforeEach
    void setUp() {
        admin = new Employee("Admin", "admin@bankofga.com", "$2a$10$hashedvalue");
        admin.setId("emp-1");
    }

    @Test
    void login_returnsEmployee_whenCredentialsValid() {
        when(employeeRepository.findByEmail("admin@bankofga.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("admin123", admin.getPasswordHash())).thenReturn(true);

        Employee result = employeeService.login(new EmployeeLoginRequest("admin@bankofga.com", "admin123"));

        assertThat(result.getId()).isEqualTo("emp-1");
        assertThat(result.getEmail()).isEqualTo("admin@bankofga.com");
    }

    @Test
    void login_throwsAuthException_whenEmailNotFound() {
        when(employeeRepository.findByEmail("ghost@bankofga.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.login(new EmployeeLoginRequest("ghost@bankofga.com", "any")))
                .isInstanceOf(AuthException.class)
                .hasMessage("invalid credentials");
    }

    @Test
    void login_throwsAuthException_whenPasswordWrong() {
        when(employeeRepository.findByEmail("admin@bankofga.com")).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> employeeService.login(new EmployeeLoginRequest("admin@bankofga.com", "wrong")))
                .isInstanceOf(AuthException.class)
                .hasMessage("invalid credentials");
    }

    @Test
    void listAllCustomers_returnsEveryCustomer() {
        Customer c1 = new Customer(); c1.setId("c1"); c1.setUsername("alice");
        Customer c2 = new Customer(); c2.setId("c2"); c2.setUsername("bob");
        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = employeeService.listAllCustomers();

        assertThat(result).hasSize(2).extracting(Customer::getUsername).containsExactly("alice", "bob");
    }

    @Test
    void listAllCustomers_returnsEmpty_whenNoCustomers() {
        when(customerRepository.findAll()).thenReturn(List.of());

        assertThat(employeeService.listAllCustomers()).isEmpty();
    }
}
