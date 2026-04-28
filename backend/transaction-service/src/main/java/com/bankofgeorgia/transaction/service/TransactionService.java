package com.bankofgeorgia.transaction.service;

import com.bankofgeorgia.transaction.dto.AccountResponse;
import com.bankofgeorgia.transaction.dto.DepositRequest;
import com.bankofgeorgia.transaction.exception.AccountServiceException;
import com.bankofgeorgia.transaction.exception.TransactionNotFoundException;
import com.bankofgeorgia.transaction.model.Transaction;
import com.bankofgeorgia.transaction.model.TransactionType;
import com.bankofgeorgia.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
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
    private final String accountServiceUrl;

    public TransactionService(TransactionRepository repository,
                              RestTemplate restTemplate,
                              @Value("${account.service.url}") String accountServiceUrl) {
        this.repository = repository;
        this.restTemplate = restTemplate;
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
