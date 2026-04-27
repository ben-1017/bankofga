package com.bankofgeorgia.customer.dto;

import com.bankofgeorgia.customer.model.Employee;

public record EmployeeResponse(
        String id,
        String name,
        String email
) {
    public static EmployeeResponse from(Employee e) {
        return new EmployeeResponse(e.getId(), e.getName(), e.getEmail());
    }
}
