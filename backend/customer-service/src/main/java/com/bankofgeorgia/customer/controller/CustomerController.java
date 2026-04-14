package com.bankofgeorgia.customer.controller;

import com.bankofgeorgia.customer.dto.CustomerResponse;
import com.bankofgeorgia.customer.dto.LoginRequest;
import com.bankofgeorgia.customer.dto.RegisterRequest;
import com.bankofgeorgia.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        CustomerResponse body = CustomerResponse.from(customerService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(CustomerResponse.from(customerService.login(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> profile(@PathVariable String id) {
        return ResponseEntity.ok(CustomerResponse.from(customerService.findById(id)));
    }
}
