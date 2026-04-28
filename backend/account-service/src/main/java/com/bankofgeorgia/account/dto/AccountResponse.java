package com.bankofgeorgia.account.dto;

import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        String id,
        String customerId,
        String productId,
        String accountNumber,
        BigDecimal balance,
        AccountStatus status,
        Instant openedAt
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(
                a.getId(), a.getCustomerId(), a.getProductId(),
                a.getAccountNumber(), a.getBalance(), a.getStatus(), a.getOpenedAt()
        );
    }
}
