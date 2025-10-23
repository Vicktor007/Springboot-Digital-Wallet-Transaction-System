package com.vic.walletservice.Mappers;


import com.vic.walletservice.Models.Wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * this ensures that the necessary details are sent to the entity to be saved
 */
public class WalletMapper {


    public static Wallet toModel(String userId) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCreatedAt(LocalDateTime.now());
        wallet.setUpdatedAt(LocalDateTime.now());
        return wallet;
    }
}
