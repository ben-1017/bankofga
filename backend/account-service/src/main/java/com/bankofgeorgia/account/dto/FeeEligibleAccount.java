package com.bankofgeorgia.account.dto;

import com.bankofgeorgia.account.model.Account;

/**
 * Minimal projection consumed by scheduler-service's monthly fee assessment job.
 * Includes productId so the scheduler can charge only Checking products.
 */
public record FeeEligibleAccount(String accountId, String customerId, String productId) {
    public static FeeEligibleAccount from(Account a) {
        return new FeeEligibleAccount(a.getId(), a.getCustomerId(), a.getProductId());
    }
}
