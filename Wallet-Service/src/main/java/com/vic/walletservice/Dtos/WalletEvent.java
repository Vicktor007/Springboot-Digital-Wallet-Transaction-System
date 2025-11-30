package com.vic.walletservice.Dtos;

import com.vic.walletservice.Enums.EventTypes;
import com.vic.walletservice.Enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public record WalletEvent(
        EventTypes eventType,
        String walletId,
        String userId,
        BigDecimal amount,
        String senderId,
        String receiverId,
        String transactionId,
        TransactionType transactionType,
        LocalDateTime timeStamp
) {}
