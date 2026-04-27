package com.bankofgeorgia.notification.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ApplyMonthlyFeeEvent(
        String accountId,
        String customerId,
        BigDecimal feeAmount,
        Instant occurredAt
) {}
