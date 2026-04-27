package com.bankofgeorgia.account.service;

import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.exception.AccountNotFoundException;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.repository.AccountRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account open(OpenAccountRequest request) {
        Account account = new Account();
        account.setCustomerId(request.customerId());
        account.setProductId(request.productId());
        account.setAccountNumber("BOG" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        account.setBalance(request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);
        account.setOpenedAt(Instant.now());
        return repository.save(account);
    }

    public Account findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
    }

    public Page<Account> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Account updateBalance(String id, BalanceUpdateRequest request) {
        Account account = findById(id);
        account.setBalance(account.getBalance().add(request.delta()));
        return repository.save(account);
    }
}
