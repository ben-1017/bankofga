package com.bankofgeorgia.account.event;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Emitted when an ACTIVE account's balance crosses below the maintenance
 * threshold. Field names mirror notification-service's
 * LowMaintenanceNotificationEvent so the JSON deserializes there directly.
 */
public record LowBalanceEvent(
        String accountId,
        String customerId,
        BigDecimal balance,
        BigDecimal threshold,
        Instant occurredAt
) {}
