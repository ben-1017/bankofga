package com.bankofgeorgia.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmployeeLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
