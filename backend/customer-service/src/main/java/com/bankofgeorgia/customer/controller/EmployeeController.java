package com.bankofgeorgia.customer.controller;

import com.bankofgeorgia.customer.dto.CustomerResponse;
import com.bankofgeorgia.customer.dto.EmployeeLoginRequest;
import com.bankofgeorgia.customer.dto.EmployeeResponse;
import com.bankofgeorgia.customer.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public ResponseEntity<EmployeeResponse> login(@Valid @RequestBody EmployeeLoginRequest request) {
        return ResponseEntity.ok(EmployeeResponse.from(employeeService.login(request)));
    }

    @GetMapping("/customers")
    public ResponseEntity<List<CustomerResponse>> customers() {
        List<CustomerResponse> list = employeeService.listAllCustomers().stream()
                .map(CustomerResponse::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/customers/{id}")
    public ResponseEntity<CustomerResponse> customer(@PathVariable String id) {
        return ResponseEntity.ok(CustomerResponse.from(employeeService.getCustomerById(id)));
    }
}
