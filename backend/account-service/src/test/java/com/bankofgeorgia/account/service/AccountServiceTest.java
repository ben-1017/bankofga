package com.bankofgeorgia.account.service;

import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.event.LowBalanceEvent;
import com.bankofgeorgia.account.event.LowBalanceEventPublisher;
import com.bankofgeorgia.account.exception.AccountNotActiveException;
import com.bankofgeorgia.account.exception.AccountNotFoundException;
import com.bankofgeorgia.account.exception.InsufficientFundsException;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final BigDecimal THRESHOLD = new BigDecimal("100.00");

    @Mock private AccountRepository repository;
    @Mock private LowBalanceEventPublisher lowBalanceEventPublisher;
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        accountService = new AccountService(repository, lowBalanceEventPublisher, THRESHOLD);
    }

    private Account activeAccount(String id, BigDecimal balance) {
        Account a = new Account();
        a.setId(id);
        a.setBalance(balance);
        a.setStatus(AccountStatus.ACTIVE);
        return a;
    }

    @Test
    void open_savesAccountWithGeneratedNumber() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.open(new OpenAccountRequest("cust-1", "prod-1", new BigDecimal("500")));

        assertThat(result.getAccountNumber()).startsWith("BOG");
        assertThat(result.getBalance()).isEqualByComparingTo("500");
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void open_defaultsBalanceToZero_whenNoInitialDeposit() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.open(new OpenAccountRequest("cust-1", "prod-1", null));

        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        when(repository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.findById("bad-id"))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("account not found");
    }

    @Test
    void updateBalance_creditsCorrectly() {
        Account account = activeAccount("acc-1", new BigDecimal("100"));
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("50")));

        assertThat(result.getBalance()).isEqualByComparingTo("150");
    }

    @Test
    void updateBalance_debitsCorrectly_whenDeltaNegative() {
        Account account = activeAccount("acc-1", new BigDecimal("100"));
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("-40")));

        assertThat(result.getBalance()).isEqualByComparingTo("60");
    }

    @Test
    void updateBalance_throwsIllegalArgument_whenDeltaIsZero() {
        assertThatThrownBy(() -> accountService.updateBalance("acc-1", new BalanceUpdateRequest(BigDecimal.ZERO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("delta must be non-zero");
    }

    @Test
    void updateBalance_throwsInsufficientFunds_whenBalanceGoesNegative() {
        Account account = activeAccount("acc-1", new BigDecimal("30"));
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("-50"))))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("insufficient funds");
    }

    @Test
    void updateBalance_throwsAccountNotActive_whenFrozen() {
        Account account = activeAccount("acc-1", new BigDecimal("100"));
        account.setStatus(AccountStatus.FROZEN);
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("50"))))
                .isInstanceOf(AccountNotActiveException.class)
                .hasMessage("account is not active");
    }

    @Test
    void findByCustomer_returnsAllAccountsForCustomer() {
        Account a1 = activeAccount("acc-1", new BigDecimal("100"));
        a1.setCustomerId("cust-1");
        Account a2 = activeAccount("acc-2", new BigDecimal("250"));
        a2.setCustomerId("cust-1");
        when(repository.findByCustomerId("cust-1")).thenReturn(List.of(a1, a2));

        List<Account> result = accountService.findByCustomer("cust-1");

        assertThat(result).extracting(Account::getId).containsExactly("acc-1", "acc-2");
    }

    @Test
    void findByCustomer_returnsEmptyList_whenCustomerHasNoAccounts() {
        when(repository.findByCustomerId("cust-none")).thenReturn(List.of());

        List<Account> result = accountService.findByCustomer("cust-none");

        assertThat(result).isEmpty();
    }

    @Test
    void findFeeEligible_returnsActiveAccountsBelowThreshold() {
        BigDecimal threshold = new BigDecimal("100.00");
        Account low = activeAccount("acc-low", new BigDecimal("12.00"));
        low.setCustomerId("cust-1");
        when(repository.findByStatusAndBalanceLessThan(AccountStatus.ACTIVE, threshold))
                .thenReturn(List.of(low));

        List<Account> result = accountService.findFeeEligible(threshold);

        assertThat(result).extracting(Account::getId).containsExactly("acc-low");
        assertThat(result).extracting(Account::getCustomerId).containsExactly("cust-1");
    }

    @Test
    void findFeeEligible_returnsEmpty_whenNoAccountsBelowThreshold() {
        BigDecimal threshold = new BigDecimal("100.00");
        when(repository.findByStatusAndBalanceLessThan(AccountStatus.ACTIVE, threshold))
                .thenReturn(List.of());

        assertThat(accountService.findFeeEligible(threshold)).isEmpty();
    }

    @Test
    void updateBalance_throwsAccountNotActive_whenClosed() {
        Account account = activeAccount("acc-1", new BigDecimal("100"));
        account.setStatus(AccountStatus.CLOSED);
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("10"))))
                .isInstanceOf(AccountNotActiveException.class);
    }

    @Test
    void updateBalance_publishesLowBalanceEvent_whenCrossingBelowThreshold() {
        Account account = activeAccount("acc-1", new BigDecimal("150.00"));
        account.setCustomerId("cust-1");
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("-70.00")));

        ArgumentCaptor<LowBalanceEvent> captor = ArgumentCaptor.forClass(LowBalanceEvent.class);
        verify(lowBalanceEventPublisher).publish(captor.capture());
        LowBalanceEvent event = captor.getValue();
        assertThat(event.accountId()).isEqualTo("acc-1");
        assertThat(event.customerId()).isEqualTo("cust-1");
        assertThat(event.balance()).isEqualByComparingTo("80.00");
        assertThat(event.threshold()).isEqualByComparingTo(THRESHOLD);
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    void updateBalance_doesNotPublish_whenBalanceStaysAtOrAboveThreshold() {
        Account account = activeAccount("acc-1", new BigDecimal("200.00"));
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("50.00")));

        verify(lowBalanceEventPublisher, never()).publish(any());
    }

    @Test
    void updateBalance_doesNotPublish_whenAlreadyBelowThreshold() {
        Account account = activeAccount("acc-1", new BigDecimal("80.00"));
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("-10.00")));

        verify(lowBalanceEventPublisher, never()).publish(any());
    }
}
