package com.bankofgeorgia.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BalanceUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", message = "delta must be non-zero") BigDecimal delta
) {}
