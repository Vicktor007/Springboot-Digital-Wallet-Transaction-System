package com.vic.walletservice.Exceptions;



public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String message) {
        super(message);
    }

    public static DuplicateTransactionException forId(String walletId) {
        return new DuplicateTransactionException("Duplicate transaction for wallet with id: " + walletId);
    }
}
