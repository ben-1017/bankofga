package com.bankofgeorgia.customer.dto;

import com.bankofgeorgia.customer.model.Customer;

public record CustomerResponse(
        String id,
        String name,
        String email,
        String username,
        String phone
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getUsername(), c.getPhone());
    }
}
