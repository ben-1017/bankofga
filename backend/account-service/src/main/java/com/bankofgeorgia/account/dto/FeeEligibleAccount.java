package com.bankofgeorgia.account.dto;

import com.bankofgeorgia.account.model.Account;

/**
 * Minimal projection consumed by scheduler-service's monthly fee assessment job.
 * Field names mirror scheduler-service's AccountSnapshot record so the JSON maps directly.
 */
public record FeeEligibleAccount(String accountId, String customerId) {
    public static FeeEligibleAccount from(Account a) {
        return new FeeEligibleAccount(a.getId(), a.getCustomerId());
    }
}
