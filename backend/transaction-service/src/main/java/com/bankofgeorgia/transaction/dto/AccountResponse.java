package com.bankofgeorgia.transaction.dto;

import java.math.BigDecimal;

public record AccountResponse(
        String id,
        String customerId,
        String accountNumber,
        BigDecimal balance,
        String status
) {}
