package com.bankofgeorgia.transaction.service;

import com.bankofgeorgia.transaction.dto.AccountResponse;
import com.bankofgeorgia.transaction.dto.DepositRequest;
import com.bankofgeorgia.transaction.exception.TransactionNotFoundException;
import com.bankofgeorgia.transaction.model.Transaction;
import com.bankofgeorgia.transaction.model.TransactionType;
import com.bankofgeorgia.transaction.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        AccountResponse account = restTemplate.getForObject(
                accountServiceUrl + "/api/accounts/" + request.accountId(), AccountResponse.class);

        Transaction tx = new Transaction();
        tx.setAccountId(request.accountId());
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(request.amount());
        tx.setBalanceBefore(account.balance());
        tx.setBalanceAfter(account.balance().add(request.amount()));
        tx.setDescription(request.description());
        tx.setCreatedAt(Instant.now());
        repository.save(tx);

        restTemplate.put(accountServiceUrl + "/api/accounts/" + request.accountId() + "/balance",
                Map.of("delta", request.amount()));

        return tx;
    }

    public List<Transaction> findByAccount(String accountId) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    public Transaction findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException("transaction not found"));
    }
}
