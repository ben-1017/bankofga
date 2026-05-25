package com.bankofgeorgia.scheduler.job;

import com.bankofgeorgia.scheduler.account.AccountQueryClient;
import com.bankofgeorgia.scheduler.account.AccountSnapshot;
import com.bankofgeorgia.scheduler.event.ApplyMonthlyFeeEvent;
import com.bankofgeorgia.scheduler.event.FeeEventPublisher;
import com.bankofgeorgia.scheduler.fee.FeeApplicator;
import com.bankofgeorgia.scheduler.product.ProductQueryClient;
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
    @Mock private ProductQueryClient productQueryClient;
    @Mock private FeeApplicator feeApplicator;
    @Mock private FeeEventPublisher feeEventPublisher;

    private static final BigDecimal FEE = new BigDecimal("5.00");

    @Test
    void run_appliesFeeThenPublishesOneEventPerSuccessfulAccount() {
        List<AccountSnapshot> eligible = List.of(
                new AccountSnapshot("acct-1", "cust-1", "prod-checking-1"),
                new AccountSnapshot("acct-2", "cust-2", "prod-savings"),
                new AccountSnapshot("acct-3", "cust-3", "prod-checking-2")
        );
        when(accountQueryClient.findFeeEligibleAccounts()).thenReturn(eligible);
        when(productQueryClient.isCheckingProduct("prod-checking-1")).thenReturn(true);
        when(productQueryClient.isCheckingProduct("prod-savings")).thenReturn(false);
        when(productQueryClient.isCheckingProduct("prod-checking-2")).thenReturn(true);
        when(feeApplicator.apply("acct-1", FEE)).thenReturn(true);
        when(feeApplicator.apply("acct-3", FEE)).thenReturn(true);

        FeeAssessmentJob job = new FeeAssessmentJob(accountQueryClient, productQueryClient, feeApplicator, feeEventPublisher, FEE);
        job.run();

        ArgumentCaptor<ApplyMonthlyFeeEvent> captor = ArgumentCaptor.forClass(ApplyMonthlyFeeEvent.class);
        verify(productQueryClient, times(3)).isCheckingProduct(org.mockito.ArgumentMatchers.anyString());
        verify(feeApplicator, times(2)).apply(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(FEE));
        verify(feeEventPublisher, times(2)).publish(captor.capture());

        List<ApplyMonthlyFeeEvent> published = captor.getAllValues();
        assertThat(published).extracting(ApplyMonthlyFeeEvent::accountId)
                .containsExactly("acct-1", "acct-3");
        assertThat(published).allSatisfy(e -> {
            assertThat(e.feeAmount()).isEqualByComparingTo(FEE);
            assertThat(e.occurredAt()).isNotNull();
        });
    }

    @Test
    void run_publishesNothing_whenNoEligibleAccounts() {
        when(accountQueryClient.findFeeEligibleAccounts()).thenReturn(List.of());

        FeeAssessmentJob job = new FeeAssessmentJob(accountQueryClient, productQueryClient, feeApplicator, feeEventPublisher, FEE);
        job.run();

        verify(productQueryClient, never()).isCheckingProduct(org.mockito.ArgumentMatchers.anyString());
        verify(feeApplicator, never()).apply(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        verify(feeEventPublisher, never()).publish(org.mockito.ArgumentMatchers.any());
    }
}
