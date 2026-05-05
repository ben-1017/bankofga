package com.bankofgeorgia.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;

public record WithdrawNotificationEvent(
        String transactionId,
        String accountId,
        String customerId,
        BigDecimal amount,
        BigDecimal balanceAfter,
        Instant occurredAt
) {}
