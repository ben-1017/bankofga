package com.bankofgeorgia.account.controller;

import com.bankofgeorgia.account.dto.FeeEligibleAccount;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.service.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final BigDecimal THRESHOLD = new BigDecimal("100.00");

    @Mock
    private AccountService accountService;

    private Account account(String id, String customerId, String balance) {
        Account a = new Account();
        a.setId(id);
        a.setCustomerId(customerId);
        a.setProductId("prod-" + id);
        a.setBalance(new BigDecimal(balance));
        a.setStatus(AccountStatus.ACTIVE);
        return a;
    }

    @Test
    void feeEligible_mapsServiceResultsToProjection_andPassesConfiguredThreshold() {
        when(accountService.findFeeEligible(THRESHOLD)).thenReturn(List.of(
                account("acc-1", "cust-1", "12.00"),
                account("acc-2", "cust-2", "0.00")));

        AccountController controller = new AccountController(accountService, THRESHOLD);
        ResponseEntity<List<FeeEligibleAccount>> response = controller.feeEligible();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(
                new FeeEligibleAccount("acc-1", "cust-1", "prod-acc-1"),
                new FeeEligibleAccount("acc-2", "cust-2", "prod-acc-2"));

        ArgumentCaptor<BigDecimal> thresholdCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        org.mockito.Mockito.verify(accountService).findFeeEligible(thresholdCaptor.capture());
        assertThat(thresholdCaptor.getValue()).isEqualByComparingTo(THRESHOLD);
    }

    @Test
    void feeEligible_returnsEmptyOkBody_whenNoEligibleAccounts() {
        when(accountService.findFeeEligible(THRESHOLD)).thenReturn(List.of());

        AccountController controller = new AccountController(accountService, THRESHOLD);
        ResponseEntity<List<FeeEligibleAccount>> response = controller.feeEligible();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
