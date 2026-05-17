package com.bankofgeorgia.customer.dto;

import com.bankofgeorgia.customer.model.Employee;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmployeeResponse(
        String id,
        String name,
        String email,
        String token
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail(), null);
    }

    public static EmployeeResponse from(Employee e, String token) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail(), token);
    }
}
