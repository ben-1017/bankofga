package com.bankofgeorgia.account.service;

import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.exception.AccountNotActiveException;
import com.bankofgeorgia.account.exception.AccountNotFoundException;
import com.bankofgeorgia.account.exception.InsufficientFundsException;
import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.event.LowBalanceEvent;
import com.bankofgeorgia.account.event.LowBalanceEventPublisher;
import com.bankofgeorgia.account.model.AccountStatus;
import com.bankofgeorgia.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Value;
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
    private final LowBalanceEventPublisher lowBalanceEventPublisher;
    private final BigDecimal lowBalanceThreshold;

    public AccountService(AccountRepository repository,
                          LowBalanceEventPublisher lowBalanceEventPublisher,
                          @Value("${app.fee.balance-threshold:100.00}") BigDecimal lowBalanceThreshold) {
        this.repository = repository;
        this.lowBalanceEventPublisher = lowBalanceEventPublisher;
        this.lowBalanceThreshold = lowBalanceThreshold;
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

    /**
     * Accounts eligible for the monthly maintenance fee: ACTIVE accounts whose
     * current balance is below the supplied threshold. Note: the data model has
     * no daily-balance history or product-type, so this uses current balance as
     * a pragmatic stand-in for "daily balance below the fee threshold".
     */
    public List<Account> findFeeEligible(BigDecimal balanceThreshold) {
        return repository.findByStatusAndBalanceLessThan(AccountStatus.ACTIVE, balanceThreshold);
    }

    public Account updateBalance(String id, BalanceUpdateRequest request) {
        if (request.delta().signum() == 0) {
            throw new IllegalArgumentException("delta must be non-zero");
        }

        Account account = findById(id);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("account is not active");
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(request.delta());
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException("insufficient funds");
        }

        account.setBalance(newBalance);
        Account saved = repository.save(account);

        // Alert once, only when the balance crosses from at/above the threshold
        // to below it — avoids re-alerting on every further sub-threshold debit.
        if (oldBalance.compareTo(lowBalanceThreshold) >= 0
                && newBalance.compareTo(lowBalanceThreshold) < 0) {
            lowBalanceEventPublisher.publish(new LowBalanceEvent(
                    saved.getId(), saved.getCustomerId(), newBalance,
                    lowBalanceThreshold, Instant.now()));
        }
        return saved;
    }
}
