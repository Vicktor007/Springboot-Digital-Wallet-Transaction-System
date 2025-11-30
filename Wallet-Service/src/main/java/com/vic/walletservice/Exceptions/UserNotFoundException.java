package com.vic.walletservice.Exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException forId(String walletId) {
        return new UserNotFoundException("User not found with ID: " + walletId);
    }
}