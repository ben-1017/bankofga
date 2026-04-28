package com.bankofgeorgia.scheduler.account;

import java.util.List;

public interface AccountQueryClient {
    List<AccountSnapshot> findFeeEligibleAccounts();
}
