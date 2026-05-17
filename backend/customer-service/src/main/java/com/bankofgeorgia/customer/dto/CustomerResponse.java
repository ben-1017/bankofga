package com.bankofgeorgia.customer.dto;

import com.bankofgeorgia.customer.model.Customer;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CustomerResponse(
        String id,
        String name,
        String email,
        String username,
        String phone,
        String token
) {
    public static CustomerResponse from(Customer c) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getUsername(), c.getPhone(), null);
    }

    public static CustomerResponse from(Customer c, String token) {
        return new CustomerResponse(c.getId(), c.getName(), c.getEmail(), c.getUsername(), c.getPhone(), token);
    }
}
