package com.bankofgeorgia.notification.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LowMaintenanceNotificationEvent(
        String accountId,
        String customerId,
        BigDecimal balance,
        BigDecimal threshold,
        Instant occurredAt
) {}
