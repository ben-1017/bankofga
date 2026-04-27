package com.bankofgeorgia.account.repository;

import com.bankofgeorgia.account.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AccountRepository extends MongoRepository<Account, String> {
    List<Account> findByCustomerId(String customerId);
    Page<Account> findAll(Pageable pageable);
}
