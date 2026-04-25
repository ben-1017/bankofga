package com.bankofgeorgia.customer.service;

import com.bankofgeorgia.customer.dto.EmployeeLoginRequest;
import com.bankofgeorgia.customer.exception.AuthException;
import com.bankofgeorgia.customer.exception.CustomerNotFoundException;
import com.bankofgeorgia.customer.model.Customer;
import com.bankofgeorgia.customer.model.Employee;
import com.bankofgeorgia.customer.repository.CustomerRepository;
import com.bankofgeorgia.customer.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Employee login(EmployeeLoginRequest request) {
        Employee employee = employeeRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthException("invalid credentials"));
        if (!passwordEncoder.matches(request.password(), employee.getPasswordHash())) {
            throw new AuthException("invalid credentials");
        }
        return employee;
    }

    public List<Customer> listAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("customer not found"));
    }
}
