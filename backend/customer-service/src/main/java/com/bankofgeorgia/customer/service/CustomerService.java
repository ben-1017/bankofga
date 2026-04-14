package com.bankofgeorgia.customer.service;

import com.bankofgeorgia.customer.dto.LoginRequest;
import com.bankofgeorgia.customer.dto.RegisterRequest;
import com.bankofgeorgia.customer.exception.AuthException;
import com.bankofgeorgia.customer.exception.DuplicateCustomerException;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.repository.CustomerRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CustomerService(CustomerRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public Customer register(RegisterRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new DuplicateCustomerException("username already taken");
        }
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateCustomerException("email already registered");
        }

        Customer customer = new Customer();
        customer.setName(request.name());
        customer.setEmail(request.email());
        customer.setUsername(request.username());
        customer.setPhone(request.phone());
        customer.setPasswordHash(passwordEncoder.encode(request.password()));
        customer.setCreatedAt(Instant.now());
        return repository.save(customer);
    }

    public Customer login(LoginRequest request) {
        Customer customer = repository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("invalid credentials"));
        if (!passwordEncoder.matches(request.password(), customer.getPasswordHash())) {
            throw new AuthException("invalid credentials");
        }
        return customer;
    }

    public Customer findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new AuthException("customer not found"));
    }
}
