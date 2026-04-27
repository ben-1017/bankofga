package com.bankofgeorgia.product.dto;

import com.bankofgeorgia.product.model.Product;
import com.bankofgeorgia.product.model.ProductType;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        String id,
        String code,
        ProductType type,
        String name,
        String description,
        BigDecimal minimumBalance,
        BigDecimal monthlyFee,
        BigDecimal interestRate,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(
                p.getId(), p.getCode(), p.getType(), p.getName(), p.getDescription(),
                p.getMinimumBalance(), p.getMonthlyFee(), p.getInterestRate(),
                p.isActive(), p.getCreatedAt(), p.getUpdatedAt()
        );
    }
}
