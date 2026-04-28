package com.bankofgeorgia.transaction.service;

import com.bankofgeorgia.transaction.dto.AccountResponse;
import com.bankofgeorgia.transaction.dto.DepositRequest;
import com.bankofgeorgia.transaction.exception.AccountServiceException;
import com.bankofgeorgia.transaction.exception.TransactionNotFoundException;
import com.bankofgeorgia.transaction.model.Transaction;
import com.bankofgeorgia.transaction.model.TransactionType;
import com.bankofgeorgia.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository repository;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transactionService, "accountServiceUrl", "http://localhost:8083");
    }

    @Test
    void deposit_recordsTransactionWithCorrectBalances() {
        AccountResponse before = new AccountResponse("acc-1", "BOG123", new BigDecimal("100"), "ACTIVE");
        AccountResponse after  = new AccountResponse("acc-1", "BOG123", new BigDecimal("150"), "ACTIVE");

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
