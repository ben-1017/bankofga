package com.bankofgeorgia.transaction.dto;

import com.bankofgeorgia.transaction.model.Transaction;
import com.bankofgeorgia.transaction.model.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(), t.getAccountId(), t.getType(), t.getAmount(),
                t.getBalanceBefore(), t.getBalanceAfter(), t.getDescription(), t.getCreatedAt()
        );
    }
}
