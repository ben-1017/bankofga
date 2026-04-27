package com.bankofgeorgia.product.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
        @NotNull Boolean active
) {}
