package com.bankofgeorgia.transaction.exception;

public class AccountServiceException extends RuntimeException {
    public AccountServiceException(String message) {
        super(message);
    }
}
