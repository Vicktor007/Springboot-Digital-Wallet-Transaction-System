package com.vic.walletservice.Dtos;

import com.vic.walletservice.Enums.EventTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletResponse(
        EventTypes eventType,
        String walletId,
        String userId,
        BigDecimal balance,
        LocalDateTime timestamp
) {
}
