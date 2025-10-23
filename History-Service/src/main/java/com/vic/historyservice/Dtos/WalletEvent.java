package com.vic.historyservice.Dtos;

import com.vic.historyservice.Enums.EventTypes;
import com.vic.historyservice.Enums.TransactionType;

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
        TransactionType  transactionType,
        LocalDateTime timeStamp
) {
}
