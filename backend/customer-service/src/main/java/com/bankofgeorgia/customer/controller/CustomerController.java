package com.bankofgeorgia.customer.controller;

import com.bankofgeorgia.customer.dto.CustomerResponse;
import com.bankofgeorgia.customer.dto.LoginRequest;
import com.bankofgeorgia.customer.dto.RegisterRequest;
import com.bankofgeorgia.customer.dto.UpdateCustomerRequest;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.security.JwtService;
import com.bankofgeorgia.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final JwtService jwtService;

    public CustomerController(CustomerService customerService, JwtService jwtService) {
        this.customerService = customerService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody RegisterRequest request) {
        CustomerResponse body = CustomerResponse.from(customerService.register(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<CustomerResponse> login(@Valid @RequestBody LoginRequest request) {
        Customer customer = customerService.login(request);
        return ResponseEntity.ok(CustomerResponse.from(customer, jwtService.issueCustomerToken(customer)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> profile(@PathVariable String id) {
        return ResponseEntity.ok(CustomerResponse.from(customerService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable String id,
                                                   @Valid @RequestBody UpdateCustomerRequest request) {
        return ResponseEntity.ok(CustomerResponse.from(customerService.updateCustomer(id, request)));
    }
}
