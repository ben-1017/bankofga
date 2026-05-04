package com.bankofgeorgia.transaction.controller;

import com.bankofgeorgia.transaction.dto.DepositRequest;
import com.bankofgeorgia.transaction.dto.TransactionResponse;
import com.bankofgeorgia.transaction.dto.WithdrawRequest;
import com.bankofgeorgia.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transactionService.deposit(request)));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransactionResponse.from(transactionService.withdraw(request)));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> byAccount(@PathVariable String accountId) {
        List<TransactionResponse> body = transactionService.findByAccount(accountId).stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> get(@PathVariable String id) {
        return ResponseEntity.ok(TransactionResponse.from(transactionService.findById(id)));
    }
}
