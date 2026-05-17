package com.bankofgeorgia.account.repository;

import com.bankofgeorgia.account.model.Account;
import com.bankofgeorgia.account.model.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.List;

public interface AccountRepository extends MongoRepository<Account, String> {
    List<Account> findByCustomerId(String customerId);
    Page<Account> findAll(Pageable pageable);
    List<Account> findByStatusAndBalanceLessThan(AccountStatus status, BigDecimal balance);
}
