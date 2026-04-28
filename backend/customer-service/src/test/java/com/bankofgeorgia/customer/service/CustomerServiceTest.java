package com.bankofgeorgia.customer.service;

import com.bankofgeorgia.customer.dto.UpdateCustomerRequest;
import com.bankofgeorgia.customer.exception.CustomerNotFoundException;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
}
