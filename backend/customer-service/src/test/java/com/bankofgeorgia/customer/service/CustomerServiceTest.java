package com.bankofgeorgia.customer.service;

import com.bankofgeorgia.customer.dto.LoginRequest;
import com.bankofgeorgia.customer.dto.RegisterRequest;
import com.bankofgeorgia.customer.dto.UpdateCustomerRequest;
import com.bankofgeorgia.customer.exception.AuthException;
import com.bankofgeorgia.customer.exception.CustomerNotFoundException;
import com.bankofgeorgia.customer.exception.DuplicateCustomerException;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private CustomerRepository repository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private CustomerService customerService;

    private Customer existing;

    @BeforeEach
    void setUp() {
        existing = new Customer();
        existing.setId("c-1");
        existing.setName("Ada Lovelace");
        existing.setEmail("ada@example.com");
        existing.setUsername("ada");
    }

    @Test
    void findById_returnsCustomer_whenIdExists() {
        when(repository.findById("c-1")).thenReturn(Optional.of(existing));

        Customer result = customerService.findById("c-1");

        assertThat(result.getId()).isEqualTo("c-1");
        assertThat(result.getEmail()).isEqualTo("ada@example.com");
    }

    @Test
    void findById_throwsCustomerNotFound_whenIdMissing() {
        when(repository.findById("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById("ghost"))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("ghost");
    }

    @Test
    void updateCustomer_updatesFields_whenCustomerExists() {
        when(repository.findById("c-1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.updateCustomer("c-1",
                new UpdateCustomerRequest("Grace Hopper", "grace@example.com", "555-9999"));

        assertThat(result.getName()).isEqualTo("Grace Hopper");
        assertThat(result.getEmail()).isEqualTo("grace@example.com");
        assertThat(result.getPhone()).isEqualTo("555-9999");
    }

    @Test
    void updateCustomer_throwsCustomerNotFound_whenIdMissing() {
        when(repository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer("bad-id",
                new UpdateCustomerRequest("X", null, null)))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void register_normalizesUsernameAndEmail_andHashesPassword() {
        when(repository.existsByUsername("alice")).thenReturn(false);
        when(repository.existsByEmail("alice@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret-12")).thenReturn("HASHED");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer saved = customerService.register(new RegisterRequest(
                "Alice Smith", "  Alice@Example.COM ", "  ALICE  ", "555-0100", "secret-12"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(repository).save(captor.capture());
        Customer persisted = captor.getValue();
        assertThat(persisted.getUsername()).isEqualTo("alice");
        assertThat(persisted.getEmail()).isEqualTo("alice@example.com");
        assertThat(persisted.getPasswordHash()).isEqualTo("HASHED");
        assertThat(saved.getUsername()).isEqualTo("alice");
    }

    @Test
    void register_rejectsDuplicateUsername_regardlessOfCase() {
        when(repository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(new RegisterRequest(
                "Alice Smith", "alice@example.com", "Alice", "555-0100", "secret-12")))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("username");
        verify(repository, never()).save(any());
    }

    @Test
    void register_rejectsDuplicateEmail_regardlessOfCase() {
        when(repository.existsByUsername("bob")).thenReturn(false);
        when(repository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.register(new RegisterRequest(
                "Bob Jones", "ALICE@Example.com", "bob", "555-0200", "secret-12")))
                .isInstanceOf(DuplicateCustomerException.class)
                .hasMessageContaining("email");
        verify(repository, never()).save(any());
    }

    @Test
    void login_normalizesUsername_andMatchesStoredHash() {
        existing.setPasswordHash("HASHED");
        when(repository.findByUsername("ada")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("pw", "HASHED")).thenReturn(true);

        Customer result = customerService.login(new LoginRequest("  ADA  ", "pw"));

        assertThat(result.getId()).isEqualTo("c-1");
    }

    @Test
    void login_rejectsInvalidPassword() {
        existing.setPasswordHash("HASHED");
        when(repository.findByUsername("ada")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrong", "HASHED")).thenReturn(false);

        assertThatThrownBy(() -> customerService.login(new LoginRequest("ada", "wrong")))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void updateCustomer_normalizesEmailWhenProvided() {
        when(repository.findById("c-1")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Customer result = customerService.updateCustomer("c-1",
                new UpdateCustomerRequest(null, "  GRACE@Example.com  ", null));

        assertThat(result.getEmail()).isEqualTo("grace@example.com");
    }
}
