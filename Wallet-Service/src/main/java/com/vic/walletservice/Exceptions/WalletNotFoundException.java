package com.vic.walletservice.Exceptions;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException(String message) {
        super(message);
    }

    public static WalletNotFoundException forId(String walletId) {
        return new WalletNotFoundException("Wallet not found with ID: " + walletId);
    }
}