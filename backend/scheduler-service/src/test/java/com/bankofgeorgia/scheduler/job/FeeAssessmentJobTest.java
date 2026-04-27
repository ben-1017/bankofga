package com.bankofgeorgia.scheduler.job;

import com.bankofgeorgia.scheduler.account.AccountQueryClient;
import com.bankofgeorgia.scheduler.account.AccountSnapshot;
import com.bankofgeorgia.scheduler.event.ApplyMonthlyFeeEvent;
import com.bankofgeorgia.scheduler.event.FeeEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeAssessmentJobTest {

    @Mock private AccountQueryClient accountQueryClient;
    @Mock private FeeEventPublisher feeEventPublisher;

    private static final BigDecimal FEE = new BigDecimal("5.00");

    @Test
    void run_publishesOneEventPerEligibleAccount() {
        List<AccountSnapshot> eligible = List.of(
                new AccountSnapshot("acct-1", "cust-1"),
                new AccountSnapshot("acct-2", "cust-2"),
                new AccountSnapshot("acct-3", "cust-3")
        );
        when(accountQueryClient.findFeeEligibleAccounts()).thenReturn(eligible);

        FeeAssessmentJob job = new FeeAssessmentJob(accountQueryClient, feeEventPublisher, FEE);
        job.run();

        ArgumentCaptor<ApplyMonthlyFeeEvent> captor = ArgumentCaptor.forClass(ApplyMonthlyFeeEvent.class);
        verify(feeEventPublisher, times(3)).publish(captor.capture());

        List<ApplyMonthlyFeeEvent> published = captor.getAllValues();
        assertThat(published).extracting(ApplyMonthlyFeeEvent::accountId)
                .containsExactly("acct-1", "acct-2", "acct-3");
        assertThat(published).allSatisfy(e -> {
            assertThat(e.feeAmount()).isEqualByComparingTo(FEE);
            assertThat(e.occurredAt()).isNotNull();
        });
    }

    @Test
    void run_publishesNothing_whenNoEligibleAccounts() {
        when(accountQueryClient.findFeeEligibleAccounts()).thenReturn(List.of());

        FeeAssessmentJob job = new FeeAssessmentJob(accountQueryClient, feeEventPublisher, FEE);
        job.run();

        verify(feeEventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}
