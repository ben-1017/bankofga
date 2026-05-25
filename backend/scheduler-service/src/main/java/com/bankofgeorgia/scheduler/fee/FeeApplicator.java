package com.bankofgeorgia.scheduler.fee;

import java.math.BigDecimal;

public interface FeeApplicator {
    boolean apply(String accountId, BigDecimal amount);
}
