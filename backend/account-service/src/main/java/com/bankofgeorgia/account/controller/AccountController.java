package com.bankofgeorgia.account.controller;

import com.bankofgeorgia.account.dto.AccountResponse;
import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.FeeEligibleAccount;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;
    private final BigDecimal feeBalanceThreshold;

    public AccountController(AccountService accountService,
                             @Value("${app.fee.balance-threshold:100.00}") BigDecimal feeBalanceThreshold) {
        this.accountService = accountService;
        this.feeBalanceThreshold = feeBalanceThreshold;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(accountService.open(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.findById(id)));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getByCustomer(@PathVariable String customerId) {
        List<AccountResponse> accounts = accountService.findByCustomer(customerId).stream()
                .map(AccountResponse::from)
                .toList();
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AccountResponse> result = accountService.findAll(PageRequest.of(page, size))
                .map(AccountResponse::from);
        return ResponseEntity.ok(result);
    }

    // Service-to-service only (scheduler-service monthly fee job). The API gateway
    // blocks any /api/**/internal/** path, so this is not reachable by external clients.
    @GetMapping("/internal/fee-eligible")
    public ResponseEntity<List<FeeEligibleAccount>> feeEligible() {
        List<FeeEligibleAccount> result = accountService.findFeeEligible(feeBalanceThreshold).stream()
                .map(FeeEligibleAccount::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> updateBalance(@PathVariable String id,
                                                         @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(AccountResponse.from(accountService.updateBalance(id, request)));
    }
}
