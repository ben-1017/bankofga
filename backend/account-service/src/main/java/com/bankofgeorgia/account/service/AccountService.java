package com.bankofgeorgia.account.service;

import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.exception.AccountNotActiveException;
import com.bankofgeorgia.account.exception.AccountNotFoundException;
import com.bankofgeorgia.account.exception.InsufficientFundsException;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.repository.AccountRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account open(OpenAccountRequest request) {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                Account account = new Account();
                account.setCustomerId(request.customerId());
                account.setProductId(request.productId());
                account.setAccountNumber("BOG" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
                account.setBalance(request.initialDeposit() != null ? request.initialDeposit() : BigDecimal.ZERO);
                account.setStatus(AccountStatus.ACTIVE);
                account.setOpenedAt(Instant.now());
                return repository.save(account);
            } catch (DuplicateKeyException ignored) {
                // account number collision — retry with a new UUID
            }
        }
        throw new IllegalStateException("failed to generate unique account number");
    }

    public Account findById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("account not found"));
    }

    public Page<Account> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public List<Account> findByCustomer(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Account updateBalance(String id, BalanceUpdateRequest request) {
        Account account = findById(id);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("account is not active");
        }

        BigDecimal newBalance = account.getBalance().add(request.delta());
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException("insufficient funds");
        }

        account.setBalance(newBalance);
        return repository.save(account);
    }
}
