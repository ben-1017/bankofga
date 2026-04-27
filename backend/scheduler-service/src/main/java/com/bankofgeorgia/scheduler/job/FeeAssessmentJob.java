package com.bankofgeorgia.scheduler.job;

import com.bankofgeorgia.scheduler.account.AccountQueryClient;
import com.bankofgeorgia.scheduler.account.AccountSnapshot;
import com.bankofgeorgia.scheduler.event.ApplyMonthlyFeeEvent;
import com.bankofgeorgia.scheduler.event.FeeEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Component
public class FeeAssessmentJob {

    private static final Logger log = LoggerFactory.getLogger(FeeAssessmentJob.class);

    private final AccountQueryClient accountQueryClient;
    private final FeeEventPublisher feeEventPublisher;
    private final BigDecimal feeAmount;

    public FeeAssessmentJob(AccountQueryClient accountQueryClient,
                            FeeEventPublisher feeEventPublisher,
                            @Value("${app.fee.amount}") BigDecimal feeAmount) {
        this.accountQueryClient = accountQueryClient;
        this.feeEventPublisher = feeEventPublisher;
        this.feeAmount = feeAmount;
    }

    @Scheduled(cron = "${app.fee.cron}")
    public void run() {
        List<AccountSnapshot> eligible = accountQueryClient.findFeeEligibleAccounts();
        if (eligible.isEmpty()) {
            log.info("Fee assessment: no eligible accounts");
            return;
        }

        Instant now = Instant.now();
        for (AccountSnapshot account : eligible) {
            ApplyMonthlyFeeEvent event = new ApplyMonthlyFeeEvent(
                    account.accountId(), account.customerId(), feeAmount, now);
            feeEventPublisher.publish(event);
        }
        log.info("Fee assessment: published {} APPLY_MONTHLY_FEE_EVENT(s)", eligible.size());
    }
}
