package com.bankofgeorgia.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record OpenAccountRequest(
        @NotBlank String customerId,
        @NotBlank String productId,
        @PositiveOrZero BigDecimal initialDeposit
) {}
