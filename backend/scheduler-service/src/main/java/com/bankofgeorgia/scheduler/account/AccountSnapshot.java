package com.bankofgeorgia.scheduler.account;

public record AccountSnapshot(
        String accountId,
        String customerId,
        String productId
) {}
