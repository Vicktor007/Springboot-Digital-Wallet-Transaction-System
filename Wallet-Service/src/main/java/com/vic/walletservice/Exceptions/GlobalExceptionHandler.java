package com.vic.walletservice.Exceptions;


import com.vic.walletservice.Services.walletService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles global validation and request-related exceptions.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);


    /**
     * Handles validation errors triggered by @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.error(errors.toString());
        return ResponseEntity.badRequest().body(errors);
    }

    /**
     * Handles IllegalArgumentExceptions (e.g., invalid enum values).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        log.error(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles wallet not found errors.
     */
    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFound(WalletNotFoundException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        log.error(response.toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }



    /**
     * Handles invalid amount errors.
     */
    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAmount(InvalidAmountException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        log.error(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles custom GlobalException.
     */
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<Map<String, String>> handleGlobalException(GlobalException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        log.error(response.toString());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Catch-all for any unhandled exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "An unexpected error occurred");
        log.error(response.toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * custom global exception class.
     */
    public static class GlobalException extends RuntimeException {
        public GlobalException(String message) {
            super(message);
        }

        public static GlobalException forError(String error) {
            log.error(error);
            return new GlobalException(error);
        }
    }
}