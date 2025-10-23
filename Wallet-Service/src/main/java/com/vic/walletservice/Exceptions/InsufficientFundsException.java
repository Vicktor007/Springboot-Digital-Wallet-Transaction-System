package com.vic.walletservice.Exceptions;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public static InsufficientFundsException forWallet(String walletId, BigDecimal currentBalance, BigDecimal requiredAmount) {
        return new InsufficientFundsException(
                String.format("Insufficient funds in wallet %s. Current: %s, Required: %s",
                        walletId, currentBalance, requiredAmount)
        );
    }
}