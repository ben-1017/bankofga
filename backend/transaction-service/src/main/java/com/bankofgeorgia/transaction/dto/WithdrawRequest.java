package com.bankofgeorgia.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record WithdrawRequest(
        @NotBlank String accountId,
        @NotNull @Positive BigDecimal amount,
        @Size(max = 255) String description
) {}
