package com.bankofgeorgia.account.controller;

import com.bankofgeorgia.account.dto.AccountResponse;
import com.bankofgeorgia.account.dto.BalanceUpdateRequest;
import com.bankofgeorgia.account.dto.OpenAccountRequest;
import com.bankofgeorgia.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(accountService.open(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(AccountResponse.from(accountService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<AccountResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AccountResponse> result = accountService.findAll(PageRequest.of(page, size))
                .map(AccountResponse::from);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<AccountResponse> updateBalance(@PathVariable String id,
                                                         @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(AccountResponse.from(accountService.updateBalance(id, request)));
    }
}
