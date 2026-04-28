package com.bankofgeorgia.account.service;

import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.exception.AccountNotActiveException;
import com.bankofgeorgia.account.exception.AccountNotFoundException;
import com.bankofgeorgia.account.exception.InsufficientFundsException;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock private AccountRepository repository;
    @InjectMocks private AccountService accountService;

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
    void updateBalance_throwsAccountNotActive_whenClosed() {
        Account account = activeAccount("acc-1", new BigDecimal("100"));
        account.setStatus(AccountStatus.CLOSED);
        when(repository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.updateBalance("acc-1", new BalanceUpdateRequest(new BigDecimal("10"))))
                .isInstanceOf(AccountNotActiveException.class);
    }
}
