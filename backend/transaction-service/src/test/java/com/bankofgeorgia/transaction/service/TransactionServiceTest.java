package com.bankofgeorgia.transaction.service;

import com.bankofgeorgia.transaction.dto.AccountResponse;
import com.bankofgeorgia.transaction.dto.ApplyMonthlyFeeRequest;
import com.bankofgeorgia.transaction.dto.DepositRequest;
import com.bankofgeorgia.transaction.dto.WithdrawRequest;
import com.bankofgeorgia.transaction.event.WithdrawEventPublisher;
import com.bankofgeorgia.transaction.event.WithdrawNotificationEvent;
import com.bankofgeorgia.transaction.exception.AccountServiceException;
import com.bankofgeorgia.transaction.exception.InsufficientFundsException;
import com.bankofgeorgia.transaction.exception.TransactionNotFoundException;
import com.bankofgeorgia.transaction.model.Transaction;
import com.bankofgeorgia.transaction.model.TransactionType;
import com.bankofgeorgia.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository repository;
    @Mock private RestTemplate restTemplate;
    @Mock private WithdrawEventPublisher withdrawEventPublisher;
    @InjectMocks private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "accountServiceUrl", "http://localhost:8083");
    }

    @Test
    void deposit_recordsTransactionWithCorrectBalances() {
        AccountResponse before = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("100"), "ACTIVE");
        AccountResponse after  = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("150"), "ACTIVE");

        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(before);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(AccountResponse.class)))
                .thenReturn(ResponseEntity.ok(after));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = transactionService.deposit(new DepositRequest("acc-1", new BigDecimal("50"), "test deposit"));

        assertThat(tx.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(tx.getBalanceBefore()).isEqualByComparingTo("100");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("150");
        assertThat(tx.getAmount()).isEqualByComparingTo("50");
    }

    @Test
    void deposit_throwsAccountServiceException_whenAccountNotFound() {
        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class)))
                .thenThrow(HttpClientErrorException.NotFound.class);

        assertThatThrownBy(() -> transactionService.deposit(new DepositRequest("bad-id", new BigDecimal("50"), null)))
                .isInstanceOf(AccountServiceException.class);
    }

    @Test
    void deposit_throwsAccountServiceException_whenAccountServiceReturnsNull() {
        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(null);

        assertThatThrownBy(() -> transactionService.deposit(new DepositRequest("acc-1", new BigDecimal("50"), null)))
                .isInstanceOf(AccountServiceException.class)
                .hasMessageContaining("account not found");
    }

    @Test
    void withdraw_recordsTransactionAndPublishesEvent() {
        AccountResponse before = new AccountResponse("acc-1", "cust-9", "BOG123", new BigDecimal("100"), "ACTIVE");
        AccountResponse after  = new AccountResponse("acc-1", "cust-9", "BOG123", new BigDecimal("60"), "ACTIVE");

        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(before);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(AccountResponse.class)))
                .thenReturn(ResponseEntity.ok(after));
        when(repository.save(any())).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId("tx-99");
            return t;
        });

        Transaction tx = transactionService.withdraw(new WithdrawRequest("acc-1", new BigDecimal("40"), "atm"));

        assertThat(tx.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(tx.getBalanceBefore()).isEqualByComparingTo("100");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("60");
        assertThat(tx.getAmount()).isEqualByComparingTo("40");

        ArgumentCaptor<WithdrawNotificationEvent> evt = ArgumentCaptor.forClass(WithdrawNotificationEvent.class);
        verify(withdrawEventPublisher).publish(evt.capture());
        assertThat(evt.getValue().transactionId()).isEqualTo("tx-99");
        assertThat(evt.getValue().customerId()).isEqualTo("cust-9");
        assertThat(evt.getValue().amount()).isEqualByComparingTo("40");
        assertThat(evt.getValue().balanceAfter()).isEqualByComparingTo("60");
    }

    @Test
    void withdraw_throwsInsufficientFunds_whenAccountServiceReturns409() {
        AccountResponse before = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("10"), "ACTIVE");
        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(before);
        HttpClientErrorException conflict = HttpClientErrorException.create(
                HttpStatus.CONFLICT, "Conflict", new HttpHeaders(),
                "{\"message\":\"insufficient funds\"}".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(AccountResponse.class)))
                .thenThrow(conflict);

        assertThatThrownBy(() -> transactionService.withdraw(new WithdrawRequest("acc-1", new BigDecimal("100"), null)))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("insufficient funds");
    }

    @Test
    void withdraw_succeedsEvenIfPublisherFails() {
        AccountResponse before = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("100"), "ACTIVE");
        AccountResponse after  = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("80"), "ACTIVE");
        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(before);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(AccountResponse.class)))
                .thenReturn(ResponseEntity.ok(after));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("kafka down")).when(withdrawEventPublisher).publish(any());

        Transaction tx = transactionService.withdraw(new WithdrawRequest("acc-1", new BigDecimal("20"), null));

        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("80");
    }

    @Test
    void applyMonthlyFee_debitsAccountAndRecordsFeeTransaction_withoutWithdrawNotification() {
        AccountResponse before = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("80"), "ACTIVE");
        AccountResponse after  = new AccountResponse("acc-1", "cust-1", "BOG123", new BigDecimal("75"), "ACTIVE");

        when(restTemplate.getForObject(anyString(), eq(AccountResponse.class))).thenReturn(before);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(), eq(AccountResponse.class)))
                .thenReturn(ResponseEntity.ok(after));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaction tx = transactionService.applyMonthlyFee(
                new ApplyMonthlyFeeRequest("acc-1", new BigDecimal("5.00"), null));

        assertThat(tx.getType()).isEqualTo(TransactionType.FEE);
        assertThat(tx.getAmount()).isEqualByComparingTo("5.00");
        assertThat(tx.getBalanceBefore()).isEqualByComparingTo("80");
        assertThat(tx.getBalanceAfter()).isEqualByComparingTo("75");
        assertThat(tx.getDescription()).isEqualTo("Monthly maintenance fee");
        verify(withdrawEventPublisher, never()).publish(any());
    }

    @Test
    void findByAccount_returnsTransactionList() {
        Transaction t1 = new Transaction(); t1.setAccountId("acc-1");
        Transaction t2 = new Transaction(); t2.setAccountId("acc-1");
        when(repository.findByAccountIdOrderByCreatedAtDesc("acc-1")).thenReturn(List.of(t1, t2));

        assertThat(transactionService.findByAccount("acc-1")).hasSize(2);
    }

    @Test
    void findByAccount_returnsEmpty_whenNoTransactions() {
        when(repository.findByAccountIdOrderByCreatedAtDesc("acc-1")).thenReturn(List.of());

        assertThat(transactionService.findByAccount("acc-1")).isEmpty();
    }

    @Test
    void findById_throwsNotFound_whenMissing() {
        when(repository.findById("bad-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.findById("bad-id"))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("transaction not found");
    }

    @Test
    void findById_returnsTransaction_whenExists() {
        Transaction tx = new Transaction();
        tx.setAccountId("acc-1");
        tx.setAmount(new BigDecimal("50"));
        when(repository.findById("tx-1")).thenReturn(Optional.of(tx));

        Transaction result = transactionService.findById("tx-1");

        assertThat(result.getAccountId()).isEqualTo("acc-1");
        assertThat(result.getAmount()).isEqualByComparingTo("50");
    }
}
