package com.bankofgeorgia.transaction.service;

import com.bankofgeorgia.transaction.dto.AccountResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final RestTemplate restTemplate;
    private final WithdrawEventPublisher withdrawEventPublisher;
    private final String accountServiceUrl;

    public TransactionService(TransactionRepository repository,
                              RestTemplate restTemplate,
                              WithdrawEventPublisher withdrawEventPublisher,
                              @Value("${account.service.url}") String accountServiceUrl) {
        this.repository = repository;
        this.restTemplate = restTemplate;
        this.withdrawEventPublisher = withdrawEventPublisher;
        this.accountServiceUrl = accountServiceUrl;
    }

    public Transaction deposit(DepositRequest request) {
        // Snapshot balanceBefore
        AccountResponse before = getAccount(request.accountId());

        // Update balance first — if this fails, no phantom transaction is recorded
        AccountResponse after = updateBalance(request.accountId(), request.amount());

        // Record using authoritative post-update balance from account-service
        Transaction tx = new Transaction();
        tx.setAccountId(request.accountId());
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(request.amount());
        tx.setBalanceBefore(before.balance());
        tx.setBalanceAfter(after.balance());
        tx.setDescription(request.description());
        tx.setCreatedAt(Instant.now());
        return repository.save(tx);
    }

    public Transaction withdraw(WithdrawRequest request) {
        AccountResponse before = getAccount(request.accountId());

        // Negative delta — account-service enforces overdraft (returns 409 on insufficient funds)
        AccountResponse after = updateBalance(request.accountId(), request.amount().negate());

        Transaction tx = new Transaction();
        tx.setAccountId(request.accountId());
        tx.setType(TransactionType.WITHDRAWAL);
        tx.setAmount(request.amount());
        tx.setBalanceBefore(before.balance());
        tx.setBalanceAfter(after.balance());
        tx.setDescription(request.description());
        tx.setCreatedAt(Instant.now());
        Transaction saved = repository.save(tx);

        // Best-effort: a Kafka outage shouldn't fail the user's withdrawal
        try {
            withdrawEventPublisher.publish(new WithdrawNotificationEvent(
                    saved.getId(),
                    saved.getAccountId(),
                    after.customerId(),
                    saved.getAmount(),
                    saved.getBalanceAfter(),
                    saved.getCreatedAt()
            ));
        } catch (RuntimeException ignored) {
        }
        return saved;
    }

    private AccountResponse getAccount(String accountId) {
        try {
            AccountResponse account = restTemplate.getForObject(
                    accountServiceUrl + "/api/accounts/" + accountId, AccountResponse.class);
            if (account == null) {
                throw new AccountServiceException("account not found: " + accountId);
            }
            return account;
        } catch (HttpClientErrorException.NotFound ex) {
            throw new AccountServiceException("account not found: " + accountId);
        } catch (RestClientException ex) {
            throw new AccountServiceException("account-service unavailable");
        }
    }

    private AccountResponse updateBalance(String accountId, BigDecimal delta) {
        try {
            ResponseEntity<AccountResponse> response = restTemplate.exchange(
                    accountServiceUrl + "/api/accounts/" + accountId + "/balance",
                    HttpMethod.PUT,
                    new HttpEntity<>(Map.of("delta", delta)),
                    AccountResponse.class);
            if (response.getBody() == null) {
                throw new AccountServiceException("account-service returned no body on balance update");
            }
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            // account-service returns 409 for InsufficientFunds and AccountNotActive — surface as a domain error
            if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                throw new InsufficientFundsException(
                        ex.getResponseBodyAsString().contains("not active") ? "account not active" : "insufficient funds");
            }
            throw new AccountServiceException("balance update rejected: " + ex.getStatusCode());
        } catch (RestClientException ex) {
            throw new AccountServiceException("account-service unavailable");
        }
    }

    public List<Transaction> findByAccount(String accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public Transaction findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("transaction not found"));
    }
}
