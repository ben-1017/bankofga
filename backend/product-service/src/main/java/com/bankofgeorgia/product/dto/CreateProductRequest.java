package com.bankofgeorgia.product.dto;

import com.bankofgeorgia.product.model.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank String code,
        @NotNull ProductType type,
        @NotBlank String name,
        String description,
        @PositiveOrZero BigDecimal minimumBalance,
        @PositiveOrZero BigDecimal monthlyFee,
        @PositiveOrZero BigDecimal interestRate
) {}
