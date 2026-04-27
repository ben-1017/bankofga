package com.bankofgeorgia.scheduler.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

// TODO: replace with REST client to account-service once
// GET /api/accounts/internal/fee-eligible exists. The endpoint should return
// every Checking account whose daily balance was below the fee threshold today.
@Component
public class StubAccountQueryClient implements AccountQueryClient {

    private static final Logger log = LoggerFactory.getLogger(StubAccountQueryClient.class);

    @Override
    public List<AccountSnapshot> findFeeEligibleAccounts() {
        log.warn("StubAccountQueryClient returning empty list — wire to account-service");
        return List.of();
    }
}
